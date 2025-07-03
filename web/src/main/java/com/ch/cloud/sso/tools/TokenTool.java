package com.ch.cloud.sso.tools;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.ch.cloud.sso.captcha.util.RandomUtils;
import com.ch.cloud.sso.dto.RefreshTokenCache;
import com.ch.cloud.sso.dto.TokenCache;
import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.props.JwtProperties;
import com.ch.e.Assert;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.EncryptUtils;
import com.ch.utils.NumberUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.log4j.Log4j2;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * decs: 授权工具类
 *
 * @author zhimin
 * @since 2019/8/31
 */
@Log4j2
@Component
public class TokenTool {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public static final String AUTH_TOKEN = "sso:auth_token";

    public static final String TOKEN_CACHE = "sso:token";

    public static final String TOKENS_CACHE = "sso:tokens";

    public static final String REFRESH_TOKEN_CACHE = "sso:refresh_token";

    public static final String USER_ROLE = "sso:user_role";

    public static final String USERS_ROLES = "sso:users_roles";

    public static final String LOCK_TOKEN = "sso:lock_token:";

    public static final String REFRESHING_TOKEN = "sso:refreshing_token:";

    UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withCache(10000).build();

    /**
     * 从数据声明生成令牌
     *
     * @param claims  数据声明
     * @param expired 过期时间(ms)
     * @param secret  签名密钥
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims, Date expired, String secret) {
        // 构建JWT对象，设置自定义声明、过期时间和签名密钥
        return Jwts.builder().setClaims(claims).setExpiration(expired).signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 数据声明
     */
    private Claims getClaimsFromToken(String token, String secret) {
        Claims claims;
        try {
            // 解析token，获取其中的claims信息
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            // 解析失败，记录日志
            log.error("TOKEN: {} is invalid!", token, e);
            claims = null;
        }
        return claims;
    }

    public void parseUserAgent(String userAgent) {
        // 解析User-Agent字符串，获取所有可用字段
        UserAgent agent = uaa.parse(userAgent);
        for (String fieldName : agent.getAvailableFieldNamesSorted()) {
            // 打印每个字段名及其值
            log.info("{} = {}", fieldName, agent.getValue(fieldName));
        }
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 数据声明
     */
    private Claims getClaimsWithExpired(String token, String secret) {
        Claims claims;
        try {
            // 正常解析token
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // token已过期，依然返回claims内容
            log.warn("TOKEN is expired!{}", token, e);
            claims = e.getClaims();
        } catch (Exception e) {
            // 其他异常，返回null
            log.error("TOKEN is invalid!{}", token, e);
            claims = null;
        }
        return claims;
    }


    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        // 获取token缓存map
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        if (tokenMap.containsKey(token)) {
            // 取出TokenCache对象，返回用户名
            return tokenMap.get(token).getUsername();
        }
        return null;
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            // 获取token缓存map
            RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            TokenCache cache = tokenMap.get(token);
            if (cache == null) {
                // 缓存不存在，视为过期
                return true;
            }
            // 解析过期时间戳，判断是否早于当前时间
            return DateUtils.parseTimestamp(cache.getExpireAt()).before(new Date());
        } catch (Exception e) {
            // 异常时记录日志
            log.error("token error: {}", token, e);
            return false;
        }
    }

    /**
     * 刷新令牌
     *
     * @param tokenVo 原令牌
     */
    public void refreshToken(TokenVo tokenVo) {
        // 获取分布式锁，防止并发刷新
        RLock lock = redissonClient.getLock(LOCK_TOKEN + tokenVo.getRefreshToken());
        try {
            // 尝试加锁，最多等待5秒
            boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("{} lock status: {}", tokenVo.getRefreshToken(), locked);
            Assert.isTrue(locked, PubError.RETRY, "网络繁忙，请稍后重试......");
            // 获取刷新token缓存
            RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                    JsonJacksonCodec.INSTANCE);
            RefreshTokenCache cache2 = refreshTokenMap.get(tokenVo.getRefreshToken());
            Assert.notEmpty(cache2, PubError.EXPIRED, "刷新令牌");
            // 获取当前刷新token下的主机token缓存
            RMapCache<String, String> refreshAccessToken = redissonClient.getMapCache(
                    REFRESHING_TOKEN + tokenVo.getRefreshToken(), JsonJacksonCodec.INSTANCE);
            // 构造新的TokenCache对象，填充请求信息
            TokenCache cache1 = new TokenCache();
            fillRequestInfo(cache1);
            // 检查当前主机是否已有刷新token
            RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            if (refreshAccessToken.containsKey(cache1.getHost())) {
                // 已存在则直接返回
                String accessToken = refreshAccessToken.get(cache1.getHost());
                cache1 = tokenMap.get(accessToken);
                tokenVo.setToken(accessToken);
                tokenVo.setExpireAt(cache1.getExpireAt());
                return;
            }
            // 从刷新token缓存中提取用户信息
            UserInfo userInfo = getUserInfoFromRefreshToken(cache2);
            // 拷贝用户信息到TokenCache
            BeanUtils.copyProperties(userInfo, cache1);
            // 设置密码（密钥）
            cache1.setPassword(cache2.getSecret());
            // 生成新的访问token和过期时间
            String accessToken = UuidUtils.generateUuid();
            Date accessExpired = new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
            cache1.setExpireAt(accessExpired.getTime());
            // 存入token缓存
            tokenMap.put(accessToken, cache1, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
            tokenVo.setToken(accessToken);
            tokenVo.setExpireAt(accessExpired.getTime());
            // 缓存主机与新token的映射，短暂有效
            if (!refreshAccessToken.containsKey(cache1.getHost())) {
                refreshAccessToken.put(cache1.getHost(), accessToken, 60, TimeUnit.SECONDS);
            }
            // 维护refreshToken与accessToken的多值映射
            RSetMultimapCache<String, String> tokensCache = redissonClient.getSetMultimapCache(TOKEN_CACHE);
            tokensCache.put(tokenVo.getRefreshToken(), accessToken);
        } catch (Exception e) {
            // 刷新异常，抛出无效token错误
            log.error("refresh token error!", e);
            ExUtils.throwError(PubError.INVALID, "TOKEN " + tokenVo.getToken() + " 无效");
        } finally {
            // 释放分布式锁
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 验证令牌
     *
     * @param token       令牌
     * @param userDetails 用户
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        // 获取token缓存map
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        if (!tokenMap.containsKey(token)) {
            // token不存在，返回无效
            return false;
        }
        // 校验密码（密钥）是否一致
        return CommonUtils.isEquals(tokenMap.get(token).getPassword(),generateSecret(userDetails.getPassword()));
    }

    public UserInfo getUserInfoFromRefreshToken(String token) {
        // 获取refreshToken缓存map
        RMapCache<String, RefreshTokenCache> tokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        RefreshTokenCache cache = tokenMap.get(token);
        Assert.notNull(cache, PubError.EXPIRED, "Refresh Token");
        // 通过缓存对象获取用户信息
        return getUserInfoFromRefreshToken(cache);
    }

    public UserInfo getUserInfoFromRefreshToken(RefreshTokenCache refreshTokenCache) {
        // 解析refreshToken中的claims
        Claims claims = getClaimsFromToken(refreshTokenCache.getToken(), refreshTokenCache.getSecret());
        Assert.notNull(claims, PubError.INVALID, "Refresh Token 无效");
        // 校验refreshToken是否过期
        Assert.isFalse(DateUtils.current().after(refreshTokenCache.getExpired()), PubError.EXPIRED, "TOKEN 已过期");
        // 提取claims中的用户信息
        String username = claims.getSubject();
        String userId = claims.get("userId", String.class);
        Long roleId = claims.get("roleId", Long.class);
        Long tenantId = claims.get("tenantId", Long.class);
        // 构造UserInfo对象
        UserInfo info = new UserInfo();
        info.setUsername(username);
        info.setUserId(userId);
        info.setRoleId(roleId);
        info.setTenantId(tenantId);
        info.setExpireAt(claims.getExpiration().getTime());
        return info;
    }

    public void invalid(TokenVo tokenVo) {
        // 获取token和refreshToken缓存map
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        // 移除访问token和刷新token
        tokenMap.remove(tokenVo.getToken());
        refreshTokenMap.remove(tokenVo.getRefreshToken());
    }

    /**
     * 根据用户密钥生成刷新Token
     *
     * @param user   用户信息
     * @param secret 密钥
     * @return 登录Token
     */
    public TokenVo generateToken(UserInfo user, String secret) {
        Date current = DateUtils.current();
        // 构造JWT claims
        Map<String, Object> claims = new HashMap<>(5);
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        claims.put("tenantId", user.getTenantId());
        claims.put("created", new Date());
        // 计算访问token和刷新token的过期时间
        Date accessExpired = DateUtils.addSeconds(current, (int) jwtProperties.getTokenExpired().getSeconds());
        Date refreshExpired = DateUtils.addSeconds(current, (int) jwtProperties.getRefreshTokenExpired().getSeconds());
        // 生成JWT字符串
        String jwtToken = generateToken(claims, refreshExpired, secret);
        // 生成访问token和刷新token
        String accessToken = UuidUtils.generateUuid();
        String refreshToken = EncryptUtils.md5(jwtToken);
        log.info("generate Token time current: {}, accessExpired: {}", DateUtils.format(current),
                DateUtils.format(accessExpired));
        // 获取token和refreshToken缓存map
        RMapCache<String, TokenCache> tokenCache = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        // 克隆用户信息到TokenCache对象
        TokenCache token = BeanUtilsV2.clone(user, TokenCache.class);
        token.setExpireAt(accessExpired.getTime());
        token.setPassword(secret);
        fillRequestInfo(token);
        // 存入token缓存
        tokenCache.put(accessToken, token, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        // 构造RefreshTokenCache对象，存入缓存
        RefreshTokenCache refreshTokenCache = RefreshTokenCache.build(jwtToken, secret, user.getUsername(),
                refreshExpired);
        refreshTokenMap.put(refreshToken, refreshTokenCache, jwtProperties.getRefreshTokenExpired().getSeconds(),
                TimeUnit.SECONDS);
        // 维护refreshToken与accessToken的多值映射
        RSetMultimapCache<String, String> tokensCache = redissonClient.getSetMultimapCache(TOKEN_CACHE);
        tokensCache.put(refreshToken, accessToken);
        tokensCache.expireKey(refreshToken, jwtProperties.getRefreshTokenExpired().getSeconds(), TimeUnit.SECONDS);
        return new TokenVo(accessToken, refreshToken, accessExpired.getTime());
    }

    public String generateSecret(String password) {
        // 对密码进行Base64编码
        return TextCodec.BASE64.encode(password);
    }

    public boolean refreshUserRole(String username, Long roleId) {
        // 获取redis hash操作对象
        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
        String role = roleId == null ? "" : roleId + "";
        if (ops.hasKey(USER_ROLE, username)) {
            // 如果已存在，判断是否需要更新
            String v = ops.get(USER_ROLE, username);
            if (!CommonUtils.isEquals(role, v)) {
                ops.put(USER_ROLE, username, role);
                return true;
            }
        } else {
            // 不存在则直接写入
            ops.put(USER_ROLE, username, role);
        }
        return false;
    }

    public Long getUserRole(String username, Long defaultRole) {
        // 优先查Redisson缓存
        RMapCache<Object, Object> map = redissonClient.getMapCache(USERS_ROLES, StringCodec.INSTANCE);
        if (!map.containsKey(username)) {
            // 不存在则写入默认角色
            map.put(username, defaultRole, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        }
        // 再查redis hash
        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
        if (ops.hasKey(USER_ROLE, username)) {
            String v = ops.get(USER_ROLE, username);
            if (NumberUtils.isNumeric(v)) {
                // 转换为Long返回
                return Long.parseLong(v);
            }
        }
        return defaultRole;
    }

    private void fillRequestInfo(TokenCache tokenCache) {
        // 获取当前请求上下文
        RequestAttributes reqAttr = RequestContextHolder.getRequestAttributes();
        if (reqAttr == null) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) reqAttr).getRequest();
        // 填充host、user-agent、ip、referer等信息
        tokenCache.setHost(request.getHeader("Host"));
        tokenCache.setUserAgent(request.getHeader("User-Agent"));
        String ip = getIP(request);
        tokenCache.setClientIp(ip);
        tokenCache.setReferer(request.getHeader("Referer"));
    }

    public static final String UNKNOWN = "unknown";

    public String getIP(HttpServletRequest request) {
        // 遍历所有header，打印日志
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            log.info("header key: {}, value: {}", key, request.getHeader(key));
        }
        // 依次尝试多种代理头，获取客户端真实IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Original-Forwarded-For");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        /*if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }*/
        return ip == null ? "N/A" : ip;
    }

    public TokenVo authToken(String authCode) {
        // 获取授权码对应的刷新令牌
        RMapCache<String, String> authCodeMap = redissonClient.getMapCache(AUTH_TOKEN);
        String refreshToken = authCodeMap.get(authCode);
        Assert.notEmpty(refreshToken, PubError.EXPIRED, "授权码");
        // 用完即删，防止重用
        authCodeMap.remove(authCode);
        // 获取token和refreshToken缓存map
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        // 获取refreshToken缓存对象
        RefreshTokenCache refreshTokenCache = refreshTokenMap.get(refreshToken);
        // 生成新令牌对应的用户信息
        UserInfo userInfo = getUserInfoFromRefreshToken(refreshTokenCache);
        // 克隆用户信息到TokenCache
        TokenCache authTokenCache = BeanUtilsV2.clone(userInfo, TokenCache.class);
        fillRequestInfo(authTokenCache);
        Date current = DateUtils.current();
        // 生成新访问token和过期时间
        String accessToken = UuidUtils.generateUuid();
        Date accessExpired = DateUtils.addSeconds(current, (int) jwtProperties.getTokenExpired().getSeconds());
        authTokenCache.setExpireAt(accessExpired.getTime());
        // 存入token缓存
        tokenMap.put(accessToken, authTokenCache, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        // 构造返回对象
        TokenVo tokenVo = new TokenVo();
        tokenVo.setToken(accessToken);
        tokenVo.setRefreshToken(refreshToken);
        tokenVo.setExpireAt(authTokenCache.getExpireAt());
        // 维护refreshToken与accessToken的多值映射
        RSetMultimapCache<String, String> tokensCache = redissonClient.getSetMultimapCache(TOKEN_CACHE);
        tokensCache.put(refreshToken, accessToken);
        return tokenVo;
    }

    public static final int MAX_RETRY = 100;

    /**
     * 获取授权码
     * @param token 访问令牌
     * @param refreshToken 刷新令牌
     * @return 授权码
     */
    public String authCode(String token, String refreshToken) {
        // 获取refreshToken缓存map
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        Assert.isTrue(refreshTokenMap.containsKey(refreshToken), PubError.EXPIRED, "刷新令牌");
        // 随机生成6位授权码
        String code = RandomUtils.getRandomString(6);
        RMapCache<String, String> authMap = redissonClient.getMapCache(AUTH_TOKEN);
        int c = 0;
        // 检查授权码是否冲突，最多重试MAX_RETRY次
        while (c < MAX_RETRY) {
            if (authMap.containsKey(code)) {
                code = RandomUtils.getRandomString(6);
                c++;
            } else {
                break;
            }
        }
        Assert.isFalse(authMap.containsKey(code), PubError.RETRY, "网络繁忙，请稍后重试...");
        // 授权码与refreshToken绑定，15秒有效
        authMap.put(code, refreshToken, 15, TimeUnit.SECONDS);
        return code;
    }

    public UserInfo getUserInfoFromToken(String token) {
        // 获取token缓存map
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        Assert.isTrue(tokenMap.containsKey(token), PubError.EXPIRED, "token", token);
        // 取出TokenCache对象，克隆为UserInfo
        TokenCache tokenCache = tokenMap.get(token);
        return BeanUtilsV2.clone(tokenCache, UserInfo.class);
    }

    public boolean renew(String token) {
        // 获取token缓存map
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        TokenCache cache = tokenMap.get(token);
        if (cache == null) {
            // token不存在，续期失败
            return false;
        }
        // 重新设置token过期时间
        tokenMap.put(token, cache, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        return true;
    }
}

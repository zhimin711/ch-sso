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
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.error("TOKEN: {} is invalid!", token, e);
            claims = null;
        }
        return claims;
    }
    
    public void parseUserAgent(String userAgent) {
        UserAgent agent = uaa.parse(userAgent);
        for (String fieldName : agent.getAvailableFieldNamesSorted()) {
            log.info(fieldName + " = " + agent.getValue(fieldName));
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
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.warn("TOKEN is expired!" + token, e);
            claims = e.getClaims();
        } catch (Exception e) {
            log.error("TOKEN is invalid!" + token, e);
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
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        if (tokenMap.containsKey(token)) {
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
            RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            TokenCache cache = tokenMap.get(token);
            if (cache == null) {
                return true;
            }
            return DateUtils.parseTimestamp(cache.getExpireAt()).before(new Date());
        } catch (Exception e) {
            log.error("token error: " + token, e);
            return false;
        }
    }
    
    /**
     * 刷新令牌
     *
     * @param tokenVo 原令牌
     */
    public void refreshToken(TokenVo tokenVo) {
        RLock lock = redissonClient.getLock(LOCK_TOKEN + tokenVo.getRefreshToken());
        
        try {
            boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
            
            log.info("{} lock status: {}", tokenVo.getRefreshToken(), locked);
            Assert.isTrue(locked, PubError.RETRY, "网络繁忙，请稍后重试......");
            
            RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                    JsonJacksonCodec.INSTANCE);
            RefreshTokenCache cache2 = refreshTokenMap.get(tokenVo.getRefreshToken());
            Assert.notEmpty(cache2, PubError.EXPIRED, "刷新令牌");
            
            RMapCache<String, String> refreshAccessToken = redissonClient.getMapCache(
                    REFRESHING_TOKEN + tokenVo.getRefreshToken(), JsonJacksonCodec.INSTANCE);
            
            TokenCache cache1 = new TokenCache();
            fillRequestInfo(cache1);
            
            RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            if (refreshAccessToken.containsKey(cache1.getHost())) {
                String accessToken = refreshAccessToken.get(cache1.getHost());
                cache1 = tokenMap.get(accessToken);
                tokenVo.setToken(accessToken);
                tokenVo.setExpireAt(cache1.getExpireAt());
                return;
            }
            UserInfo userInfo = getUserInfoFromRefreshToken(cache2);
            BeanUtils.copyProperties(userInfo, cache1);
            cache1.setPassword(cache2.getSecret());
            
            String accessToken = UuidUtils.generateUuid();
            Date accessExpired = new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
            
            cache1.setExpireAt(accessExpired.getTime());
            tokenMap.put(accessToken, cache1, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
            
            tokenVo.setToken(accessToken);
            tokenVo.setExpireAt(accessExpired.getTime());
            
            if (!refreshAccessToken.containsKey(cache1.getHost())) {
                refreshAccessToken.put(cache1.getHost(), accessToken, 60, TimeUnit.SECONDS);
            }
            
            RSetMultimapCache<String, String> tokensCache = redissonClient.getSetMultimapCache(TOKEN_CACHE);
            tokensCache.put(tokenVo.getRefreshToken(), accessToken);
        } catch (Exception e) {
            log.error("refresh token error!", e);
            ExUtils.throwError(PubError.INVALID, "TOKEN " + tokenVo.getToken() + " 无效");
        } finally {
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
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        if (!tokenMap.containsKey(token)) {
            return false;
        }
        // 验证密码一致
        return CommonUtils.isEquals(tokenMap.get(token).getPassword(),generateSecret(userDetails.getPassword()));
    }
    
    public UserInfo getUserInfoFromRefreshToken(String token) {
        RMapCache<String, RefreshTokenCache> tokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        RefreshTokenCache cache = tokenMap.get(token);
        Assert.notNull(cache, PubError.EXPIRED, "Refresh Token");
        return getUserInfoFromRefreshToken(cache);
    }
    
    public UserInfo getUserInfoFromRefreshToken(RefreshTokenCache refreshTokenCache) {
        Claims claims = getClaimsFromToken(refreshTokenCache.getToken(), refreshTokenCache.getSecret());
        Assert.notNull(claims, PubError.INVALID, "Refresh Token 无效");
        Assert.isFalse(DateUtils.current().after(refreshTokenCache.getExpired()), PubError.EXPIRED, "TOKEN 已过期");
        String username = claims.getSubject();
        String userId = claims.get("userId", String.class);
        Long roleId = claims.get("roleId", Long.class);
        Long tenantId = claims.get("tenantId", Long.class);
        
        UserInfo info = new UserInfo();
        info.setUsername(username);
        info.setUserId(userId);
        info.setRoleId(roleId);
        info.setTenantId(tenantId);
        info.setExpireAt(claims.getExpiration().getTime());
        return info;
    }
    
    public void invalid(TokenVo tokenVo) {
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
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
        
        Map<String, Object> claims = new HashMap<>(5);
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        claims.put("tenantId", user.getTenantId());
        claims.put("created", new Date());
        //new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
        Date accessExpired = DateUtils.addSeconds(current, (int) jwtProperties.getTokenExpired().getSeconds());
        //new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpired().toMillis());
        Date refreshExpired = DateUtils.addSeconds(current, (int) jwtProperties.getRefreshTokenExpired().getSeconds());
        String jwtToken = generateToken(claims, refreshExpired, secret);
        
        String accessToken = UuidUtils.generateUuid();
        String refreshToken = EncryptUtils.md5(jwtToken);
        
        log.info("generate Token time current: {}, accessExpired: {}", DateUtils.format(current),
                DateUtils.format(accessExpired));
        
        RMapCache<String, TokenCache> tokenCache = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        
        TokenCache token = BeanUtilsV2.clone(user, TokenCache.class);
        token.setExpireAt(accessExpired.getTime());
        token.setPassword(secret);
        fillRequestInfo(token);
        tokenCache.put(accessToken, token, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        
        RefreshTokenCache refreshTokenCache = RefreshTokenCache.build(jwtToken, secret, user.getUsername(),
                refreshExpired);
        refreshTokenMap.put(refreshToken, refreshTokenCache, jwtProperties.getRefreshTokenExpired().getSeconds(),
                TimeUnit.SECONDS);
        
        RSetMultimapCache<String, String> tokensCache = redissonClient.getSetMultimapCache(TOKEN_CACHE);
        tokensCache.put(refreshToken, accessToken);
        tokensCache.expireKey(refreshToken, jwtProperties.getRefreshTokenExpired().getSeconds(), TimeUnit.SECONDS);
        return new TokenVo(accessToken, refreshToken, accessExpired.getTime());
    }
    
    public String generateSecret(String password) {
        return TextCodec.BASE64.encode(password);
    }
    
    public boolean refreshUserRole(String username, Long roleId) {
        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
        String role = roleId == null ? "" : roleId + "";
        if (ops.hasKey(USER_ROLE, username)) {
            String v = ops.get(USER_ROLE, username);
            if (!CommonUtils.isEquals(role, v)) {
                ops.put(USER_ROLE, username, role);
                return true;
            }
        } else {
            ops.put(USER_ROLE, username, role);
        }
        return false;
    }
    
    public Long getUserRole(String username, Long defaultRole) {
        
        RMapCache<Object, Object> map = redissonClient.getMapCache(USERS_ROLES, StringCodec.INSTANCE);
        if (!map.containsKey(username)) {
            map.put(username, defaultRole, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        }
        
        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
        if (ops.hasKey(USER_ROLE, username)) {
            String v = ops.get(USER_ROLE, username);
            if (NumberUtils.isNumeric(v)) {
                return Long.parseLong(v);
            }
        }
        return defaultRole;
    }
    
    private void fillRequestInfo(TokenCache tokenCache) {
        RequestAttributes reqAttr = RequestContextHolder.getRequestAttributes();
        if (reqAttr == null) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) reqAttr).getRequest();
        tokenCache.setHost(request.getHeader("Host"));
        tokenCache.setUserAgent(request.getHeader("User-Agent"));
        
        String ip = getIP(request);
        tokenCache.setClientIp(ip);
        tokenCache.setReferer(request.getHeader("Referer"));
    }
    
    public static final String UNKNOWN = "unknown";
    
    public String getIP(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            log.info("header key: {}, value: {}", key, request.getHeader(key));
        }
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
        authCodeMap.remove(authCode);
        
        // 获取刷新令牌对应的用户信息
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        RefreshTokenCache refreshTokenCache = refreshTokenMap.get(refreshToken);
        // 生成新令牌对应的用户信息
        UserInfo userInfo = getUserInfoFromRefreshToken(refreshTokenCache);
        TokenCache authTokenCache = BeanUtilsV2.clone(userInfo, TokenCache.class);
        fillRequestInfo(authTokenCache);
        Date current = DateUtils.current();
        // 生成新令牌及过期时间
        String accessToken = UuidUtils.generateUuid();
        Date accessExpired = DateUtils.addSeconds(current, (int) jwtProperties.getTokenExpired().getSeconds());
        authTokenCache.setExpireAt(accessExpired.getTime());
        // 缓存新令牌及过期时间
        tokenMap.put(accessToken, authTokenCache, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        // 生成返回令牌信息
        TokenVo tokenVo = new TokenVo();
        tokenVo.setToken(accessToken);
        tokenVo.setRefreshToken(refreshToken);
        tokenVo.setExpireAt(authTokenCache.getExpireAt());
        // 缓存新令牌对应的刷新令牌
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
//        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
//        Assert.isTrue(tokenMap.containsKey(token), PubError.EXPIRED, "访问令牌");
        RMapCache<String, RefreshTokenCache> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE,
                JsonJacksonCodec.INSTANCE);
        Assert.isTrue(refreshTokenMap.containsKey(refreshToken), PubError.EXPIRED, "刷新令牌");
        String code = RandomUtils.getRandomString(6);
        RMapCache<String, String> authMap = redissonClient.getMapCache(AUTH_TOKEN);
        
        int c = 0;
        while (c < MAX_RETRY) {
            if (authMap.containsKey(code)) {
                code = RandomUtils.getRandomString(6);
                c++;
            } else {
                break;
            }
        }
        Assert.isFalse(authMap.containsKey(code), PubError.RETRY, "网络繁忙，请稍后重试...");
        //        RefreshTokenCache refreshTokenCache = refreshTokenMap.get(refreshToken);
        authMap.put(code, refreshToken, 15, TimeUnit.SECONDS);
        return code;
    }
    
    public UserInfo getUserInfoFromToken(String token) {
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        Assert.isTrue(tokenMap.containsKey(token), PubError.EXPIRED, "token", token);
        TokenCache tokenCache = tokenMap.get(token);
        return BeanUtilsV2.clone(tokenCache, UserInfo.class);
    }
    
    public boolean renew(String token) {
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        TokenCache cache = tokenMap.get(token);
        if (cache == null) {
            return false;
        }
        // 重新设置过期时间
        tokenMap.put(token, cache, jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        return true;
    }
}

package com.ch.cloud.sso.tools;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.ch.cloud.sso.pojo.TokenCache;
import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.props.JwtProperties;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
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
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * decs:
 *
 * @author 01370603
 * @since 2019/8/31
 */
@Log4j2
@Component
public class JwtTokenTool {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient      redissonClient;

    public static final String USER_TOKEN          = "sso:user_token:";
    public static final String USER_REFRESH_TOKEN  = "sso:user_refresh_token:";
    public static final String TOKEN_SECRET        = "sso:token:";
    public static final String REFRESH_TOKEN       = "sso:refresh_token:";
    public static final String TOKEN_CACHE         = "sso:token";
    public static final String REFRESH_TOKEN_CACHE = "sso:refresh_token";
    public static final String USER_ROLE           = "sso:user_role";
    public static final String USERS_ROLES         = "sso:users_roles";
    public static final String LOCK_TOKEN          = "sso:lock_token:";

    /**
     * 从数据声明生成令牌
     *
     * @param claims  数据声明
     * @param expired 过期时间(ms)
     * @param secret  签名密钥
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims, Date expired, String secret) {

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expired)
                .signWith(SignatureAlgorithm.HS512, secret).
                compact();
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
            log.error("TOKEN is invalid!" + token, e);
            claims = null;
        }
        return claims;
    }


    /**
     * 从令牌中获取数据声明
     *
     * @param token  令牌
     * @param secret
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
        if (tokenMap.containsKey(token)) return tokenMap.get(token).getUsername();
        return null;
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            TokenCache cache = tokenMap.get(token);
            if (cache == null) return true;
            Claims claims = getClaimsFromToken(cache.getToken(), cache.getSecret());
            if (claims == null) return true;
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("token error: " + token, e);
            return false;
        }
    }

    /**
     * 刷新令牌
     *
     * @param tokenVo 原令牌
     * @return 新令牌
     */
    public void refreshToken(TokenVo tokenVo) {
        RLock lock = redissonClient.getLock(LOCK_TOKEN + tokenVo.getRefreshToken());
        try {

            boolean locked = lock.tryLock(5, TimeUnit.SECONDS);

            log.info("{} lock status: {}", tokenVo.getRefreshToken(), locked);
            if (!locked) {
                ExceptionUtils._throw(PubError.RETRY, "网络异常，请稍后重试......");
            }
            RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            RMapCache<String, String> refreshTokenMap = redissonClient.getMapCache(REFRESH_TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
            TokenCache cache2 = tokenMap.get(tokenVo.getRefreshToken());
            if (CommonUtils.isEmpty(cache2)) {
                ExceptionUtils._throw(PubError.EXPIRED, tokenVo.getRefreshToken());
            }
            Date current = DateUtils.current();
            TokenCache cache1 = tokenMap.get(tokenVo.getToken());
            if (cache1 != null) {//原Token过期 > 300s return original token
                Date expired = DateUtils.addSeconds(cache1.getExpired(), -300);
                if (expired.after(current)) {
                    return;
                }
            }
            if (refreshTokenMap.containsKey(tokenVo.getRefreshToken())) {
                String accessToken = refreshTokenMap.get(tokenVo.getRefreshToken());
                cache1 = tokenMap.get(accessToken);
                if (cache1 != null) {
                    tokenVo.setToken(accessToken);
                    tokenVo.setExpireAt(cache1.getExpired().getTime());
                    return;
                }
            }
            String accessToken = UuidUtils.generateUuid();
            Date accessExpired = new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
            tokenVo.setToken(accessToken);
            tokenVo.setExpireAt(accessExpired.getTime());

            tokenMap.put(accessToken, TokenCache.build(cache2.getToken(), cache2.getSecret(), cache2.getUsername(), accessExpired), jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("refresh token error!", e);
            ExceptionUtils._throw(PubError.INVALID, "TOKEN " + tokenVo.getToken() + " 无效");
        } finally {
            if (lock.isLocked()) lock.unlock();
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
        if (!tokenMap.containsKey(token)) return false;

        return (tokenMap.get(token).getSecret().equals(generateSecret(userDetails.getPassword())) && !isTokenExpired(token));
    }

    public UserInfo getUserInfoFromToken(String token) {
        RMapCache<String, TokenCache> tokenMap = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        TokenCache cache = tokenMap.get(token);
        if (cache == null) {
            throw ExceptionUtils.create(PubError.EXPIRED, "TOKEN 已过期");
        }
        Claims claims = getClaimsFromToken(cache.getToken(), cache.getSecret());
        if (claims == null) {
            throw ExceptionUtils.create(PubError.INVALID, "TOKEN 无效");
        }
        if (isTokenExpired(token)) {
            throw ExceptionUtils.create(PubError.EXPIRED, "TOKEN 已过期");
        }
        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);
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
        tokenMap.remove(tokenVo.getToken());
        tokenMap.remove(tokenVo.getRefreshToken());
    }

    /**
     * 根据用户密钥生成刷新Token
     *
     * @param user   用户信息
     * @param secret 密钥
     * @return
     */
    public TokenVo generateToken(UserInfo user, String secret) {

        String accessToken = UuidUtils.generateUuid();
        Date current = DateUtils.current();

        Map<String, Object> claims = new HashMap<>(5);
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        claims.put("tenantId", user.getTenantId());
        claims.put("created", new Date());
        Date jwtExpired = DateUtils.addSeconds(current, (int) jwtProperties.getRefreshTokenExpired().getSeconds());//new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpired().toMillis());
        String jwtToken = generateToken(claims, jwtExpired, secret);
        String refreshToken = EncryptUtils.md5(jwtToken);

        Date accessExpired = DateUtils.addSeconds(current, (int) jwtProperties.getTokenExpired().getSeconds());//new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
        log.info("generate Token time current: {}, accessExpired: {}", DateUtils.format(current), DateUtils.format(accessExpired));
        RMapCache<String, TokenCache> tokenCache = redissonClient.getMapCache(TOKEN_CACHE, JsonJacksonCodec.INSTANCE);
        tokenCache.put(accessToken, TokenCache.build(jwtToken, secret, user.getUsername(), accessExpired), jwtProperties.getTokenExpired().getSeconds(), TimeUnit.SECONDS);
        tokenCache.put(refreshToken, TokenCache.build(jwtToken, secret, user.getUsername(), jwtExpired), jwtProperties.getRefreshTokenExpired().getSeconds(), TimeUnit.SECONDS);

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
        if (map.containsKey(username)) {
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
}

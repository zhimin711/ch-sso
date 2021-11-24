package com.ch.cloud.sso.tools;

import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.props.JwtProperties;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.ch.utils.EncryptUtils;
import com.ch.utils.NumberUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/8/31
 */
@Log4j2
@Component
public class JwtTokenTool {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static final String USER_TOKEN         = "sso:user_token:";
    public static final String USER_REFRESH_TOKEN = "sso:user_refresh_token:";
    public static final String TOKEN_SECRET       = "sso:token:";
    public static final String USER_ROLE          = "sso:user_role";

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
        String username;
        try {
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String md5 = EncryptUtils.md5(token);
            String secret = ops.get(TOKEN_SECRET + md5);
            Claims claims = getClaimsFromToken(token, secret);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String md5 = EncryptUtils.md5(token);
            String secret = ops.get(TOKEN_SECRET + md5);
            Claims claims = getClaimsFromToken(token, secret);
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
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String md52 = EncryptUtils.md5(tokenVo.getRefreshToken());
        String secret = ops.get(TOKEN_SECRET + md52);
        if (CommonUtils.isEmpty(secret)) {
            ExceptionUtils._throw(PubError.EXPIRED, tokenVo.getRefreshToken());
        }
        try {

            Claims claims = getClaimsWithExpired(tokenVo.getToken(), secret);
            claims.put("created", new Date());
            Date date = new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
            String refreshedToken = generateToken(claims, date, secret);
            String md5 = EncryptUtils.md5(refreshedToken);
            String oldMd5 = ops.getAndSet(USER_TOKEN + claims.getSubject(), md5);
            if (CommonUtils.isNotEmpty(oldMd5)) {
                stringRedisTemplate.delete(TOKEN_SECRET + oldMd5);
            }
            ops.set(TOKEN_SECRET + md5, secret, jwtProperties.getTokenExpired());
            tokenVo.setToken(refreshedToken);
            tokenVo.setExpireAt(date.getTime());
        } catch (Exception e) {
            log.error("refresh token error!", e);
            throw ExceptionUtils.create(PubError.INVALID, "TOKEN " + tokenVo.getToken() + " 无效");
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
        String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(UserInfo info) {
        return generateToken(info, jwtProperties.getSecret());
    }

    public String generateRefreshToken(UserInfo info) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", info.getUsername());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpired().toMillis());
        return generateToken(claims, date, jwtProperties.getSecret());
    }

    /**
     * 根据用户生成根据刷新Token
     *
     * @param user   用户信息
     * @param secret 密钥
     * @return
     */
    public String generateRefreshToken(UserInfo user, String secret) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", user.getUsername());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpired().toMillis());

        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String token = generateToken(claims, date, secret);
        String md5 = EncryptUtils.md5(token);

        String oldMd5 = ops.getAndSet(USER_REFRESH_TOKEN + user.getUsername(), md5);
        if (CommonUtils.isNotEmpty(oldMd5)) {
            stringRedisTemplate.delete(TOKEN_SECRET + oldMd5);
        }
        ops.set(TOKEN_SECRET + md5, secret, jwtProperties.getRefreshTokenExpired());
        return token;
    }

    public UserInfo getUserInfoFromToken(String token) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String md5 = EncryptUtils.md5(token);
        String secret = ops.get(TOKEN_SECRET + md5);
        Claims claims = getClaimsFromToken(token, secret);
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

    }

    /**
     * 根据用户密钥生成刷新Token
     *
     * @param user   用户信息
     * @param secret 密钥
     * @return
     */
    public String generateToken(UserInfo user, String secret) {
        Map<String, Object> claims = new HashMap<>(5);
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        claims.put("tenantId", user.getRoleId());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
        user.setExpireAt(date.getTime());
        String token = generateToken(claims, date, secret);
        String md5 = EncryptUtils.md5(token);
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String oldMd5 = ops.getAndSet(USER_TOKEN + user.getUsername(), md5);
        if (CommonUtils.isNotEmpty(oldMd5)) {
            stringRedisTemplate.delete(TOKEN_SECRET + oldMd5);
        }
        ops.set(TOKEN_SECRET + md5, secret, jwtProperties.getTokenExpired());

        return token;
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

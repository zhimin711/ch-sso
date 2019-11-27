package com.ch.cloud.sso.tools;

import com.ch.cloud.sso.pojo.RoleVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.e.PubError;
import com.ch.utils.ExceptionUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/8/31
 */
@Data
@Log4j2
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtTokenTool implements Serializable {


    private String secret;

    private Long expired;

    private Long refreshExpired = (long) 1000 * 3600 * 24;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpired(Long expired) {
        this.expired = expired;
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims, Date expired) {
        return Jwts.builder().setClaims(claims).setExpiration(expired).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims getClaimsFromToken(String token) {
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
     * @param token 令牌
     * @return 数据声明
     */
    private Claims getClaimsWithExpired(String token) {
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
            Claims claims = getClaimsFromToken(token);
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
            Claims claims = getClaimsFromToken(token);
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
     * @param token 原令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            Claims claims = getClaimsWithExpired(token);
            claims.put("created", new Date());
            Date date = new Date(System.currentTimeMillis() + expired);
            refreshedToken = generateToken(claims, date);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
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
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("sub", info.getUsername());
        claims.put("userId", info.getUserId());
        claims.put("roleId", info.getRoleId());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + expired);
        return generateToken(claims, date);
    }

    public String generateRefreshToken(UserInfo info) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", info.getUsername());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + refreshExpired);
        return generateToken(claims, date);
    }

    public UserInfo getUserInfoFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            throw ExceptionUtils.create(PubError.INVALID, "TOKEN 无效");
        }
        if (isTokenExpired(token)) {
            throw ExceptionUtils.create(PubError.EXPIRED, "TOKEN 已过期");
        }
        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);
        Long roleId = claims.get("roleId", Long.class);
        UserInfo info = new UserInfo();
        info.setUsername(username);
        info.setUserId(userId);
        info.setRoleId(roleId);
        return info;
    }
}

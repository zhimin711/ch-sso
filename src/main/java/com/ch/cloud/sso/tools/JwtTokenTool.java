package com.ch.cloud.sso.tools;

import com.ch.cloud.sso.pojo.RoleVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.e.PubError;
import com.ch.utils.ExceptionUtils;
import io.jsonwebtoken.Claims;
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

    private Long expiration;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder().setClaims(claims).setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, secret).compact();
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
            log.error("token is invalid!" + token, e);
            claims = null;
        }
        return claims;
    }

    /**
     * 生成令牌
     *
     * @param userDetails 用户
     * @return 令牌
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", userDetails.getUsername());

        claims.put("created", new Date());
        return generateToken(claims);
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
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
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
            Claims claims = getClaimsFromToken(token);
            claims.put("created", new Date());
            refreshedToken = generateToken(claims);
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
        return generateToken(claims);
    }

    public UserInfo getUserInfoFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            throw ExceptionUtils.create(PubError.INVALID);
        }
        if(isTokenExpired(token)){
            throw ExceptionUtils.create(PubError.INVALID);
        }
        String username = claims.getSubject();
        Long userId = (Long) claims.get("userId");
        Long roleId = (Long) claims.get("roleId");
        UserInfo info = new UserInfo();
        info.setUsername(username);
        info.setUserId(userId);
        info.setUserId(roleId);
        return info;
    }
}

package com.ch.cloud.sso.tools;

import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.props.JwtProperties;
import com.ch.e.PubError;
import com.ch.e.ExceptionUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims, Date expired) {
        return Jwts.builder().setClaims(claims).setExpiration(expired).signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret()).compact();
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
            claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
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
            claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
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
     * @param tokenVo 原令牌
     * @return 新令牌
     */
    public void refreshToken(TokenVo tokenVo) {
        try {
            Claims claims = getClaimsWithExpired(tokenVo.getToken());
            claims.put("created", new Date());
            Date date = new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpired().toMillis());
            String refreshedToken = generateToken(claims, date);
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
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("sub", info.getUsername());
        claims.put("userId", info.getUserId());
        claims.put("roleId", info.getRoleId());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + jwtProperties.getTokenExpired().toMillis());
        info.setExpireAt(date.getTime());
        return generateToken(claims, date);
    }

    public String generateRefreshToken(UserInfo info) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", info.getUsername());
        claims.put("created", new Date());
        Date date = new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpired().toMillis());
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
        info.setExpireAt(claims.getExpiration().getTime());
        return info;
    }
}

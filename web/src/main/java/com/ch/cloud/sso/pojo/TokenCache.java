package com.ch.cloud.sso.pojo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * desc: redis cache token info
 *
 * @author zhimin
 * @since 2022/3/15 6:29 PM
 */
@Data
@NoArgsConstructor
public class TokenCache {
    
    /**
     * jwt token
     */
    private String token;
    
    /**
     * jwt token secret
     */
    private String secret;
    
    /**
     * username
     */
    private String username;
    
    /**
     * expired
     */
    private Date expired;
    
    /**
     * user client ip
     */
    private String clientIp;
    
    /**
     * user agent
     */
    private String userAgent;
    
    /**
     * referer host
     */
    private String referer;
    
    public TokenCache(String token, String secret, String username, Date expired) {
        this.token = token;
        this.secret = secret;
        this.username = username;
        this.expired = expired;
    }
    
    public static TokenCache build(String token, String secret, String code, Date expired) {
        return new TokenCache(token, secret, code, expired);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TokenCache that = (TokenCache) o;
        return Objects.equals(token, that.token) && Objects.equals(secret, that.secret) && Objects.equals(username,
                that.username) && Objects.equals(clientIp, that.clientIp) && Objects.equals(userAgent, that.userAgent)
                && Objects.equals(referer, that.referer);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(token, secret, username, clientIp, userAgent, referer);
    }
}
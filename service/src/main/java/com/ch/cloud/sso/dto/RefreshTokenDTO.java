package com.ch.cloud.sso.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * desc: redis cache token info
 *
 * @author zhimin
 * @since 2022/3/15 6:29 PM
 */
@Data
@NoArgsConstructor
public class RefreshTokenDTO {

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


    public RefreshTokenDTO(String token, String secret, String username, Date expired) {
        this.token = token;
        this.secret = secret;
        this.username = username;
        this.expired = expired;
    }

    public static RefreshTokenDTO build(String token, String secret, String code, Date expired) {
        return new RefreshTokenDTO(token, secret, code, expired);
    }

}
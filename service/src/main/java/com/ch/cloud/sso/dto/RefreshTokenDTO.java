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
     * jwt
     */
    private String jwt;

    /**
     * jwt secret
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


    public RefreshTokenDTO(String jwt, String secret, String username, Date expired) {
        this.jwt = jwt;
        this.secret = secret;
        this.username = username;
        this.expired = expired;
    }

    public static RefreshTokenDTO build(String jwt, String secret, String code, Date expired) {
        return new RefreshTokenDTO(jwt, secret, code, expired);
    }

}

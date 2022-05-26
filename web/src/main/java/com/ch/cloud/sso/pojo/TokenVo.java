package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * decs:
 *
 * @author 01370603
 * @since 2019/11/1
 */
@Data
@NoArgsConstructor
public class TokenVo {

    /**
     * 授权TOKEN
     */
    private String token;
    /**
     * 刷新授权TOKEN
     */
    private String refreshToken;

    private Long expireAt;

    public TokenVo(String token, String refreshToken, Long expireAt) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expireAt = expireAt;
    }
}

package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/11/1
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

    public TokenVo(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}

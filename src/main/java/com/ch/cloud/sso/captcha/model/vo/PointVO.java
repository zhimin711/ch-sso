package com.ch.cloud.sso.captcha.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * decs:坐标验证码.
 *
 * @author zhimin.ma
 * @date 2021/1/1
 */
@Data
@NoArgsConstructor
public class PointVO {

    private String secretKey;

    public int x;

    public int y;

    public PointVO(int x, int y, String secretKey) {
        this.secretKey = secretKey;
        this.x = x;
        this.y = y;
    }

    public PointVO(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

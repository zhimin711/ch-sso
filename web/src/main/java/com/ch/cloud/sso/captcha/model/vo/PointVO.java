package com.ch.cloud.sso.captcha.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * decs:坐标验证码.
 *
 * @author zhimin.ma
 * @since 2021/1/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointVO {

    public int x;

    public int y;
}

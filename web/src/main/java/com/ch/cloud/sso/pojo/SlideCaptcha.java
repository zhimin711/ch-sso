package com.ch.cloud.sso.pojo;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimi
 * @since 2020/10/18 18:37
 */
@Data
public class SlideCaptcha {

    private int x;
    private int y;

    private String origin;
    private String shade;
    private String puzzle;

}

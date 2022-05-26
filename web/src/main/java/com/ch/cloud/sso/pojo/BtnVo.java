package com.ch.cloud.sso.pojo;

import lombok.Data;

/**
 * decs:
 *
 * @author 01370603
 * @since 2019/9/10
 */
@Data
public class BtnVo {

    private String pid;
    private String code;
    private String name;

    public BtnVo(String pid, String code, String name) {
        this.pid = pid;
        this.code = code;
        this.name = name;
    }
}

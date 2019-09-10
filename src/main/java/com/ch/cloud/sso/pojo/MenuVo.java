package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/9/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MenuVo extends BtnVo {

    private String icon;

    public MenuVo(String pid, String icon, String code, String name) {
        super(pid, code, name);
        this.icon = icon;
    }
}

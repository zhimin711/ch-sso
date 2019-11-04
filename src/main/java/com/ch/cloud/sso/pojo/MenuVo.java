package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/9/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MenuVo extends BtnVo {

    private String type;
    private String icon;
    private String url;
    private Integer sort;

    public MenuVo(String pid, String icon, String code, String name) {
        super(pid, code, name);
        this.icon = icon;
    }

    private List<MenuVo> children;
}

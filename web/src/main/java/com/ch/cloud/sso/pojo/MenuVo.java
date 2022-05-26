package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @since 2019/9/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MenuVo extends BtnVo {

    /**
     * 类型(1.目录 2.菜单页 3.按钮)
     */
    private String type;
    /**
     * 图标
     */
    private String icon;
    /**
     * 地址
     */
    private String url;
    /**
     * 重定向地址
     */
    private String redirect;
    /**
     * 排序
     */
    private Integer sort;

    private boolean hidden;

    public MenuVo(String pid, String icon, String code, String name) {
        super(pid, code, name);
        this.icon = icon;
    }

    private List<MenuVo> children;
}

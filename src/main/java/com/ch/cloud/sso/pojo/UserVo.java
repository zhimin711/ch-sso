package com.ch.cloud.sso.pojo;

import com.ch.cloud.client.dto.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/9/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserVo extends UserDto {

    private String avatar;

    private Collection<RoleVo> roleList;
    private Collection<MenuVo> menuList;
    private Collection<BtnVo> btnList;

    public Collection<RoleVo> getRoleList() {
        return roleList;
    }

    public void setRoleList(Collection<RoleVo> roleList) {
        this.roleList = roleList;
    }

    public Collection<MenuVo> getMenuList() {
        return menuList;
    }

    public void setMenuList(Collection<MenuVo> menuList) {
        this.menuList = menuList;
    }

    public Collection<BtnVo> getBtnList() {
        return btnList;
    }

    public void setBtnList(Collection<BtnVo> btnList) {
        this.btnList = btnList;
    }
}

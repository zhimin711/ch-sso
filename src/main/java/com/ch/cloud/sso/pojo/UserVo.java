package com.ch.cloud.sso.pojo;

import com.ch.cloud.client.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "用户及权限信息", description = "")
@JsonIgnoreProperties({"password"})
public class UserVo extends UserDto {

    private String avatar;

    @ApiModelProperty(hidden = true)
    private String password;

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

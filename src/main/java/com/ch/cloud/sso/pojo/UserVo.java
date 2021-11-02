package com.ch.cloud.sso.pojo;

import com.ch.cloud.client.dto.TenantDto;
import com.ch.cloud.client.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
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

    private Long tenantId;
    private String tenantName;

    @ApiModelProperty(hidden = true)
    private String password;
    @ApiModelProperty(hidden = true)
    private String token;

    private Collection<RoleVo>    roleList   = new ArrayList<>();
    private Collection<MenuVo>    menuList   = new ArrayList<>();
    private Collection<BtnVo>     btnList    = new ArrayList<>();
    private Collection<TenantDto> tenantList = new ArrayList<>();

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

package com.ch.cloud.sso.pojo;

import com.ch.cloud.client.dto.TenantDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 * desc:用户权限信息
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/11/2
 */
@Data
public class UserPermissionVo {

    /**
     * 角色
     */
    private Collection<RoleVo>     roleList   = new ArrayList<>();
    /**
     * 菜单权限列表
     */
    private Collection<MenuVo>    menuList   = new ArrayList<>();
    /**
     * 按钮权限列表
     */
    private Collection<BtnVo>     btnList    = new ArrayList<>();
    /**
     *
     */
    private Collection<TenantDto> tenantList = new ArrayList<>();
}

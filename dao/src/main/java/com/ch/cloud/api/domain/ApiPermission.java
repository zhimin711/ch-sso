package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口权限对象 bt_api_permission
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "接口权限")
@TableName("bt_api_permission")
public class ApiPermission extends BaseEntity {
    @Schema(description = "上级ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "代码")
    @TableField("code")
    private String code;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "描述")
    @TableField("`desc`")
    private String desc;

    @Schema(description = "状态：0. 1.菜单 2.按钮")
    @TableField("type")
    private String type;

    @Schema(description = "角色列表")
    @TableField("roles")
    private String roles;
} 
package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ch.cloud.api.dto.EnvDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户环境配置实体
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "bt_api_tenant", autoResultMap = true)
@Schema(description = "租户环境配置实体")
public class ApiTenant {

    /**
     * 主键
     */
    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户空间ID
     */
    @Schema(description = "租户空间ID")
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 租户名称
     */
    @Schema(description = "租户名称")
    @TableField("name")
    private String name;

    /**
     * 租户描述
     */
    @Schema(description = "租户描述")
    @TableField("description")
    private String description;

    /**
     * 环境配置JSON格式
     * 只包含环境列表
     * [
     *   {
     *     "envKey": "dev",
     *     "name": "开发环境",
     *     "domain": "https://dev-api.example.com",
     *     "prefix": "/api/v1",
     *     "description": "开发环境配置",
     *     "isDefault": true
     *   }
     * ]
     */
    @Schema(description = "环境配置JSON格式")
    @TableField(value = "env", typeHandler = JacksonTypeHandler.class)
    private List<EnvDTO> envList;
    

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(value = "create_at", fill = FieldFill.INSERT)
    private LocalDateTime createAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    @TableField("create_by")
    private String createBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    @TableField("update_by")
    private String updateBy;
} 
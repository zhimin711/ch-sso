package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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
    @TableField("workspace_id")
    private Long workspaceId;

    /**
     * 环境配置名称
     */
    @Schema(description = "环境配置名称")
    @TableField("name")
    private String name;

    /**
     * 环境标识(dev/test/prod等)
     */
    @Schema(description = "环境标识")
    @TableField("env_key")
    private String envKey;

    /**
     * 环境域名
     */
    @Schema(description = "环境域名")
    @TableField("domain")
    private String domain;

    /**
     * 请求前缀
     */
    @Schema(description = "请求前缀")
    @TableField("prefix")
    private String prefix;

    /**
     * 环境描述
     */
    @Schema(description = "环境描述")
    @TableField("description")
    private String description;

    /**
     * 是否默认环境
     */
    @Schema(description = "是否默认环境")
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 环境配置JSON格式
     */
    @Schema(description = "环境配置JSON格式")
    @TableField(value = "env_config", typeHandler = JacksonTypeHandler.class)
    private Object envConfig;

    /**
     * 状态：0.失效 1.生效
     */
    @Schema(description = "状态")
    @TableField("status")
    private String status;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

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
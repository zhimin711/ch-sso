package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 租户环境配置DTO
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@Data
@Schema(description = "租户环境配置DTO")
public class ApiTenantDTO {

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 租户空间ID
     */
    @Schema(description = "租户空间ID")
    @NotNull(message = "租户空间ID不能为空")
    private Long workspaceId;

    /**
     * 环境配置名称
     */
    @Schema(description = "环境配置名称")
    @NotBlank(message = "环境配置名称不能为空")
    private String name;

    /**
     * 环境标识(dev/test/prod等)
     */
    @Schema(description = "环境标识")
    @NotBlank(message = "环境标识不能为空")
    private String envKey;

    /**
     * 环境域名
     */
    @Schema(description = "环境域名")
    @NotBlank(message = "环境域名不能为空")
    private String domain;

    /**
     * 请求前缀
     */
    @Schema(description = "请求前缀")
    private String prefix;

    /**
     * 环境描述
     */
    @Schema(description = "环境描述")
    private String description;

    /**
     * 是否默认环境
     */
    @Schema(description = "是否默认环境")
    private Boolean isDefault;

    /**
     * 环境配置JSON格式
     */
    @Schema(description = "环境配置JSON格式")
    private Object envConfig;

    /**
     * 状态：0.失效 1.生效
     */
    @Schema(description = "状态")
    private String status;
} 
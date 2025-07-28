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
     * 租户名称
     */
    @Schema(description = "租户名称")
    @NotBlank(message = "租户名称不能为空")
    private String name;

    /**
     * 租户描述
     */
    @Schema(description = "租户描述")
    private String description;

    /**
     * 环境配置JSON格式
     * 只包含环境列表
     */
    @Schema(description = "环境配置JSON格式")
    private Object env;

}

package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 合并后的环境配置DTO
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@Data
@Schema(description = "合并后的环境配置DTO")
public class MergedEnvConfigDTO {

    /**
     * 环境标识
     */
    @Schema(description = "环境标识")
    private String envKey;

    /**
     * 环境名称
     */
    @Schema(description = "环境名称")
    private String name;

    /**
     * 环境域名
     */
    @Schema(description = "环境域名")
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
     * 环境配置JSON格式
     */
    @Schema(description = "环境配置JSON格式")
    private Object envConfig;

    /**
     * 配置来源：tenant-租户配置，project-项目配置
     */
    @Schema(description = "配置来源")
    private String configSource;

    /**
     * 租户配置ID
     */
    @Schema(description = "租户配置ID")
    private Long tenantConfigId;
} 
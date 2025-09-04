package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

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
     * 租户空间ID
     */
    @Schema(description = "租户空间ID")
    @NotNull(message = "租户空间ID不能为空")
    private Long tenantId;

    /**
     * 环境配置JSON格式
     * 只包含环境列表
     */
    @Schema(description = "环境配置JSON格式")
    private List<EnvDTO> envList;

}

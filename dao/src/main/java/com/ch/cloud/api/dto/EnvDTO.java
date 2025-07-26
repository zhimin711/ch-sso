package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvDTO {

    private Long id;

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
     * 描述
 */
    @Schema(description = "描述")
    private String description;
}

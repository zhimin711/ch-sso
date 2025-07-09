package com.ch.cloud.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResultWrapDTO {

    @Schema(description = "是否启用")
    private boolean enabled;

    @Schema(description = "json path")
    private String jsonPath;

    @Schema(description = "json 模板")
    private String jsonTemplate;

    @Schema(description = "json schema")
    private String jsonSchema;
}

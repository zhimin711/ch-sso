package com.ch.cloud.json.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * desc: ApiJsonSchemaDTO
 * </p>
 * <pre>
 * {
 *   "type": "string",
 *   "title": "13123",
 *   "description": "1"
 * }
 * </pre>
 *
 * @author zhimin
 * @since 2025/6/17 09:48
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "JSON Schema Basic 数据传输对象")
public class JsonSchemaBasicDTO {
    
    @Schema(description = "字段类型", example = "string")
    private String type;
    
    @Schema(description = "标题", example = "13123")
    private String title;
    
    @Schema(description = "描述信息", example = "1")
    private String description;
    
}

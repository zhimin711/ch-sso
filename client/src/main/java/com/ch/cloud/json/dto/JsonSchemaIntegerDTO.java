package com.ch.cloud.json.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * desc: ApiJsonSchemaDTO
 * </p>
 * <pre>
 *     {
 *   "type": "integer",
 *   "title": "",
 *   "description": "123",
 *   "maximum": 3,
 *   "minimum": 1,
 *   "exclusiveMaximum": true,
 *   "exclusiveMinimum": true,
 *   "enum": [
 *     1,
 *     2
 *   ]
 * }
 * </pre>
 *
 * @author zhimin
 * @since 2025/6/17 09:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "JSON Schema Integer 数据传输对象")
public class JsonSchemaIntegerDTO extends JsonSchemaNumberDTO {
    
    public JsonSchemaIntegerDTO() {
        this.setType("integer");
    }
}

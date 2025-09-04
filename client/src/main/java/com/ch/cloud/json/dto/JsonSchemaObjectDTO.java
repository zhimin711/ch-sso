package com.ch.cloud.json.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * desc: ApiJsonSchemaDTO
 * </p>
 * <pre>
 * {
 *   "type": "string",
 *   "title": "13123",
 *   "description": "1",
 *   "maxLength": 3,
 *   "minLength": 1,
 *   "pattern": "1",
 *   "format": "date",
 *   "enum": [
 *     "1",
 *     "2"
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
@Schema(description = "JSON Schema Object 数据传输对象")
public class JsonSchemaObjectDTO extends JsonSchemaBasicDTO {
    
    public JsonSchemaObjectDTO() {
        setType("object");
    }
    
    @Schema(description = "对象属性定义")
    private Map<String, ? extends JsonSchemaBasicDTO> properties;
    
    @Schema(description = "最大元素个数")
    private Integer maxProperties;
    
    @Schema(description = "最小元素个数")
    private Integer minProperties;
    
    @Schema(description = "必填字段列表")
    private List<String> required;
}

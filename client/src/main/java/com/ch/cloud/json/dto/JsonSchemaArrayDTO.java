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
 *   "type": "array",
 *   "title": "",
 *   "items": {
 *     "type": "object",
 *     "properties": {
 *       "field_2_1": {
 *         "type": "string",
 *         "title": ""
 *       }
 *     }
 *   },
 *   "description": "123",
 *   "minItems": 1,
 *   "maxItems": 11,
 *   "uniqueItems": true
 * }
 * </pre>
 *
 * @author zhimin
 * @since 2025/6/17 09:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "JSON Schema Array 数据传输对象")
public class JsonSchemaArrayDTO extends JsonSchemaBasicDTO {
    
    public JsonSchemaArrayDTO() {
        setType("array");
    }
    
    @Schema(description = "数组元素")
    private JsonSchemaBasicDTO items; // 修改: 将泛型声明改为具体的类型
    
    @Schema(description = "最大元素个数")
    private Integer maxItems;
    
    @Schema(description = "最小元素个数")
    private Integer minItems;
    
    @Schema(description = "开启后，每个元素都不相同")
    private Boolean uniqueItems;
}
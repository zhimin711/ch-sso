package com.ch.cloud.json.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * desc: JsonSchemaNumberDTO
 * </p>
 * <pre>
 *     {
 *   "type": "number",
 *   "title": "",
 *   "description": "123",
 *   "maximum": 11,
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
@Schema(description = "JSON Schema Number 数据传输对象")
public class JsonSchemaNumberDTO extends JsonSchemaBasicDTO {
    
    public JsonSchemaNumberDTO()
    {
        setType("number");
    }
    
    @Schema(description = "数值最大值")
    private Number maximum;
    
    @Schema(description = "数值最小值")
    private Number minimum;
    
    @Schema(description = "开启后，数据必须小于最大值")
    private Boolean exclusiveMaximum;
    
    @Schema(description = "开启后，数据必须大于最小值")
    private Boolean exclusiveMinimum;
    
    @Schema(description = "枚举值列表", example = "[1, 2]")
    @JsonAlias("enum")
    private List<Number> enumList;
    
}

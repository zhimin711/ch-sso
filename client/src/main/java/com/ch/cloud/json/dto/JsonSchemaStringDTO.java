package com.ch.cloud.json.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

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
@Schema(description = "JSON Schema String 数据传输对象", example = "{\n  \"type\": \"string\",\n  \"title\": \"13123\",\n  \"description\": \"1\",\n  \"maxLength\": 3,\n  \"minLength\": 1,\n  \"pattern\": \"1\",\n  \"format\": \"date\",\n  \"enum\": [\"1\", \"2\"]\n}")
public class JsonSchemaStringDTO extends JsonSchemaBasicDTO {
    
    public JsonSchemaStringDTO() {
        setType("string");
    }
    
    @Schema(description = "最大长度限制", example = "3")
    private Integer maxLength;
    
    @Schema(description = "最小长度限制", example = "1")
    private Integer minLength;
    
    @Schema(description = "正则表达式规则", example = "1")
    private String pattern;
    
    @Schema(description = "数据格式（如 date、email 等）", example = "date")
    private String format;
    
    @Schema(description = "枚举值列表", example = "[\"1\", \"2\"]")
    @JsonAlias("enum")
    private List<String> enumList;
    
}

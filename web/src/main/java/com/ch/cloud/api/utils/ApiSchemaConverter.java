package com.ch.cloud.api.utils;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.json.dto.JsonSchemaArrayDTO;
import com.ch.cloud.json.dto.JsonSchemaBasicDTO;
import com.ch.cloud.json.dto.JsonSchemaIntegerDTO;
import com.ch.cloud.json.dto.JsonSchemaNumberDTO;
import com.ch.cloud.json.dto.JsonSchemaObjectDTO;
import com.ch.cloud.json.dto.JsonSchemaStringDTO;
import com.ch.cloud.api.domain.ApiSchema;
import com.ch.cloud.api.dto.ApiParamDTO;
import com.ch.cloud.api.pojo.DocParameter;
import com.ch.cloud.json.enums.SchemaType;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * desc: API Schema转换工具类
 * </p>
 * <p>
 * 用于将OpenAPI 3.0的parameters转换成JSON Schema对象结构， 以满足前端json-schema-editor编辑需求
 * </p>
 *
 * @author zhimin
 * @since 2025/1/20
 */
@Slf4j
public class ApiSchemaConverter {
    
    public static String parsePathParams2ApiParams(List<DocParameter> parameters, Map<String, ApiSchema> schemaMap) {
        
        if (CommonUtils.isEmpty(parameters)) {
            return null;
        }
        JSONObject params = new JSONObject();
        Map<String, List<DocParameter>> paramGroups = parameters.stream()
                .collect(java.util.stream.Collectors.groupingBy(DocParameter::getIn));
        
        // 处理header参数
        if (paramGroups.containsKey("header")) {
            List<DocParameter> headers = paramGroups.get("header");
            List<ApiParamDTO> paramList = headers.stream().map(ApiSchemaConverter::convertApiParamDTO)
                    .collect(Collectors.toList());
            params.put("headers", paramList);
        }
        if (paramGroups.containsKey("cookie")) {
            List<DocParameter> headers = paramGroups.get("cookie");
            List<ApiParamDTO> paramList = headers.stream().map(ApiSchemaConverter::convertApiParamDTO)
                    .collect(Collectors.toList());
            params.put("cookie", paramList);
        }
        if (paramGroups.containsKey("path")) {
            List<DocParameter> headers = paramGroups.get("path");
            List<ApiParamDTO> paramList = headers.stream().map(ApiSchemaConverter::convertApiParamDTO)
                    .collect(Collectors.toList());
            params.put("path", paramList);
        }
        if (paramGroups.containsKey("query")) {
            List<DocParameter> parameterList = paramGroups.get("query");
            
            List<ApiParamDTO> paramList = parameterList.stream()
                    .filter(param -> CommonUtils.isEmpty(param.getSchema()) || !param.getSchema().containsKey("$ref"))
                    .map(ApiSchemaConverter::convertApiParamDTO).collect(Collectors.toList());
            
            parameterList.stream()
                    .filter(param -> CommonUtils.isNotEmpty(param.getSchema()) && param.getSchema().containsKey("$ref"))
                    .forEach(param -> {
                        String refKey = param.getSchema().getString("$ref");
                        JsonSchemaObjectDTO rootSchema = ApiSchemaConverter.resolveSchemaReference(refKey, schemaMap,
                                Lists.newArrayList());
                        if (CommonUtils.isEmpty(rootSchema) || CommonUtils.isEmpty(rootSchema.getProperties())) {
                            return;
                        }
                        rootSchema.getProperties().forEach((key, value) -> {
                            ApiParamDTO dto = new ApiParamDTO();
                            // 使用key作为参数名称，这是schema中属性的名称
                            dto.setName(key);
                            dto.setDescription(value.getDescription());
                            dto.setType(value.getType());
                            // 设置是否必填
                            dto.setRequired(rootSchema.getRequired() != null && rootSchema.getRequired().contains(key));
                            // 设置格式
                            if (value instanceof JsonSchemaStringDTO) {
                                JsonSchemaStringDTO stringDTO = (JsonSchemaStringDTO) value;
                                if (CommonUtils.isNotEmpty(stringDTO.getFormat())) {
                                    dto.setFormat(stringDTO.getFormat());
                                }
                            }
                            paramList.add(dto);
                        });
                    });
            params.put("query", paramList);
        }
        return params.toString();
    }
    
    private static ApiParamDTO convertApiParamDTO(DocParameter param) {
        ApiParamDTO dto = BeanUtil.copyProperties(param, ApiParamDTO.class);
        JSONObject schema = param.getSchema();
        if (CommonUtils.isNotEmpty(schema)) {
            if (schema.containsKey("type")) {
                dto.setType(schema.getString("type"));
            }
            if (schema.containsKey("format")) {
                dto.setFormat(schema.getString("format"));
            }
            if (schema.containsKey("example")) {
                dto.setExample(schema.getString("example"));
            }
        }
        return dto;
    }
    
    
    /**
     * 解析schema引用
     */
    private static JsonSchemaObjectDTO resolveSchemaReference(String refKey, Map<String, ApiSchema> schemaMap,
            List<String> refKeys) {
        if (CommonUtils.isEmpty(refKey)) {
            return null;
        }
        
        // 处理不同的引用格式
        String normalizedKey = refKey;
        if (!schemaMap.containsKey(normalizedKey)) {
            if (refKey.startsWith("#/definitions/")) {
                normalizedKey = refKey.substring("#/definitions/".length());
            } else if (refKey.startsWith("#/components/schemas/")) {
                normalizedKey = refKey.substring("#/components/schemas/".length());
            }
        }
        
        JsonSchemaObjectDTO schema = new JsonSchemaObjectDTO();
        schema.setType("object");
        
        ApiSchema apiSchema = CommonUtils.isEmpty(schemaMap) ? null : schemaMap.get(normalizedKey);
        if (apiSchema == null) {
            // 如果找不到引用的schema，返回一个基本的引用对象
            return schema;
        }
        schema.setType(apiSchema.getType());
        schema.setTitle(apiSchema.getTitle());
        schema.setDescription(apiSchema.getDescription());
        if (CommonUtils.isNotEmpty(apiSchema.getRequired())) {
            if (JSON.isValidArray(apiSchema.getRequired())) {
                schema.setRequired(JSON.parseArray(apiSchema.getRequired(), String.class));
            }
        }
        // 如果有properties，递归解析
        if (CommonUtils.isEmpty(apiSchema.getProperties()) || refKeys.contains(normalizedKey)) {
            return schema;
        } else {
            refKeys.add(normalizedKey);
        }
        
        try {
            JSONObject propertiesJson = JSON.parseObject(apiSchema.getProperties());
            Map<String, JsonSchemaBasicDTO> properties = Maps.newHashMap();
            
            for (String key : propertiesJson.keySet()) {
                JSONObject propJson = propertiesJson.getJSONObject(key);
                String type = propJson.getString("type");
                JsonSchemaBasicDTO propSchema = new JsonSchemaBasicDTO();
                if (CommonUtils.isEquals(type, "array")) {
                    propSchema = new JsonSchemaArrayDTO();
                    JSONObject items = propJson.getJSONObject("items");
                    if (items != null) {
                        if (items.containsKey("$ref")) {
                            String subRefKey = items.getString("$ref");
                            ((JsonSchemaArrayDTO) propSchema).setItems(
                                    resolveSchemaReference(subRefKey, schemaMap, refKeys));
                        } else {
                            ((JsonSchemaArrayDTO) propSchema).setItems(items.to(JsonSchemaBasicDTO.class));
                        }
                    }
                } else if (CommonUtils.isEquals(type, "string")) {
                    propSchema = new JsonSchemaStringDTO();
                    JsonSchemaStringDTO propSchema1 = (JsonSchemaStringDTO) propSchema;
                    if (propJson.containsKey("enum")) {
                        propSchema1.setEnumList(propJson.getJSONArray("enum").toList(String.class));
                    }
                    if (propJson.containsKey("format")) {
                        String format = propJson.getString("format");
                        if (format.equals("date-time")) {
                            propSchema1.setFormat("date-time");
                        }
                        if (format.equals("date")) {
                            propSchema1.setFormat("date");
                        }
                    }
                }
                propSchema.setType(type);
                propSchema.setDescription(propJson.getString("description"));
                if (!propJson.containsKey("title")) {
                    propSchema.setTitle(propSchema.getDescription());
                } else {
                    propSchema.setTitle(propJson.getString("title"));
                }
                //                propSchema.setFormat(propJson.getString("format"));
                //                propSchema.setExample(propJson.getString("example"));
                
                // 处理引用
                if (propJson.containsKey("$ref")) {
                    String subRefKey = propJson.getString("$ref");
                    propSchema = resolveSchemaReference(subRefKey, schemaMap, refKeys);
                }
                
                properties.put(key, propSchema);
            }
            
            schema.setProperties(properties);
        } catch (Exception e) {
            // 解析失败时忽略properties
            log.error("解析属性失败: {}", e.getMessage());
        }
        
        return schema;
        
    }
    
    
    /**
     * 将JSON Schema转换为JSON字符串
     *
     * @param schema JSON Schema对象
     * @return JSON字符串
     */
    public static String toJsonString(JsonSchemaBasicDTO schema) {
        if (schema == null) {
            return null;
        }
        return JSON.toJSONString(schema);
    }
    
    /**
     * 从JSON字符串解析JSON Schema对象
     *
     * @param jsonString JSON字符串
     * @return JSON Schema对象
     */
    public static JsonSchemaBasicDTO fromJsonString(String jsonString) {
        if (CommonUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return JSON.parseObject(jsonString, JsonSchemaBasicDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    
    public static String parseBody2JsonSchema(JSONObject requestBody, Map<String, ApiSchema> schemaMap) {
        if (CommonUtils.isEmpty(requestBody)) {
            return null;
        }
        if (!requestBody.containsKey("content")) {
            return null;
        }
        JSONObject content = requestBody.getJSONObject("content");
        JSONObject jsonBody = new JSONObject();
        if (content.containsKey("application/json")) {
            jsonBody.put("type", "json");
            JSONObject json = content.getJSONObject("application/json");
            if (json.containsKey("schema")) {
                JSONObject schema = json.getJSONObject("schema");
                if (schema.containsKey("$ref")) {
                    JsonSchemaObjectDTO jsonSchema = resolveSchemaReference(schema.getString("$ref"), schemaMap,
                            Lists.newArrayList());
                    jsonBody.put("root", jsonSchema);
                } else {
                    JsonSchemaBasicDTO jsonSchema = convertJsonSchema(schema, schemaMap);
                    jsonBody.put("root", jsonSchema);
                }
            }
        } else if (content.containsKey("application/x-www-form-urlencoded")) {
            jsonBody.put("type", "form");
        } else if (content.containsKey("application/form-data")) {
            jsonBody.put("type", "form");
        }
        return jsonBody.toString();
    }
    
    public static String parseParamBody2JsonSchema(DocParameter parameter, Map<String, ApiSchema> schemaMap) {
        if (CommonUtils.isEmpty(parameter.getSchema())) {
            return null;
        }
        JSONObject jsonBody = new JSONObject();
        JSONObject schema = parameter.getSchema();
        if (schema.containsKey("$ref")) {
            JsonSchemaObjectDTO jsonSchema = resolveSchemaReference(schema.getString("$ref"), schemaMap,
                    Lists.newArrayList());
            jsonBody.put("root", jsonSchema);
        } else {
            JsonSchemaBasicDTO jsonSchema = convertJsonSchema(schema, schemaMap);
            jsonBody.put("root", jsonSchema);
        }
        jsonBody.put("type", "json");
        //           if (content.containsKey("application/x-www-form-urlencoded")) {
        //            jsonBody.put("type", "form");
        //        } else if (content.containsKey("application/form-data")) {
        //            jsonBody.put("type", "form");
        //        }
        return jsonBody.toString();
    }
    
    private static JsonSchemaBasicDTO convertJsonSchema(JSONObject schema, Map<String, ApiSchema> schemaMap) {
        if (schema.containsKey("$ref")) {
            return resolveSchemaReference(schema.getString("$ref"), schemaMap, Lists.newArrayList());
        }
        
        String type = schema.getString("type");
        JsonSchemaBasicDTO jsonSchema = schema.to(JsonSchemaBasicDTO.class);
        switch (SchemaType.fromType(type)) {
            case ARRAY:
                JsonSchemaArrayDTO arrayDTO = schema.to(JsonSchemaArrayDTO.class);
                JSONObject items = schema.getJSONObject("items");
                if (items.containsKey("$ref")) {
                    arrayDTO.setItems(resolveSchemaReference(items.getString("$ref"), schemaMap, Lists.newArrayList()));
                } else {
                    arrayDTO.setItems(ApiSchemaConverter.convertJsonSchema(items, schemaMap));
                }
                return arrayDTO;
            case INTEGER:
                return schema.to(JsonSchemaIntegerDTO.class);
            case NUMBER:
                return schema.to(JsonSchemaNumberDTO.class);
            case STRING:
                return schema.to(JsonSchemaStringDTO.class);
            case BOOLEAN:
                jsonSchema.setType("boolean");
                return jsonSchema;
            case OBJECT:
                return schema.to(JsonSchemaObjectDTO.class);
        }
        return null;
    }
    
    public static String parseResponses2JsonSchema(JSONObject responses, Map<String, ApiSchema> schemaMap) {
        if (CommonUtils.isEmpty(responses)) {
            return null;
        }
        JSONObject jsonResponse = new JSONObject();
        if (responses.containsKey("200")) {
            JSONObject jsonObject = responses.getJSONObject("200");
            if (jsonObject.containsKey("content")) {
                JSONObject content = jsonObject.getJSONObject("content");
                JSONObject json = content.getJSONObject("*/*");
                if (content.containsKey("application/json")) {
                    json = content.getJSONObject("application/json");
                }
                if (json.containsKey("schema")) {
                    JsonSchemaBasicDTO schema = ApiSchemaConverter.convertJsonSchema(json.getJSONObject("schema"),
                            schemaMap);
                    jsonResponse.put("200", schema);
                }
            } else if (jsonObject.containsKey("schema")) {
                JsonSchemaBasicDTO schema = ApiSchemaConverter.convertJsonSchema(jsonObject.getJSONObject("schema"),
                        schemaMap);
                jsonResponse.put("200", schema);
            }
        }
        return jsonResponse.toString();
    }

    /**
     * 测试方法：验证schema解析是否正确
     * 
     * @param schemaJson schema的JSON字符串
     * @param schemaMap schema映射
     * @return 解析结果
     */
    public static String testSchemaParsing(String schemaJson, Map<String, ApiSchema> schemaMap) {
        try {
            JSONObject schema = JSON.parseObject(schemaJson);
            JsonSchemaObjectDTO result = resolveSchemaReference(schema.getString("$ref"), schemaMap, Lists.newArrayList());
            if (result != null && result.getProperties() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("解析结果：\n");
                result.getProperties().forEach((key, value) -> {
                    sb.append("参数名称: ").append(key)
                      .append(", 类型: ").append(value.getType())
                      .append(", 描述: ").append(value.getDescription())
                      .append("\n");
                });
                return sb.toString();
            }
            return "解析失败或没有属性";
        } catch (Exception e) {
            return "解析异常: " + e.getMessage();
        }
    }
}
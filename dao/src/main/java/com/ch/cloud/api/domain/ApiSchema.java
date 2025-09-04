package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 接口对象定义信息对象 bt_api_definition
 *
 * @author zhimin.ma
 * @since 2022-10-16 18:03:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "接口对象定义信息")
@TableName("bt_api_schema")
public class ApiSchema extends BaseEntity {

    /**
     * projectId
     */
    @Schema(description = "projectId")
    @TableField("project_id")
    private Long projectId;

    /**
     * 类型
     */
    @Schema(description = "类型")
    @TableField("type")
    private String type;

    /**
     * 标识
     */
    @Schema(description = "标识")
    @TableField("def_key")
    private String defKey;

    /**
     * 名称
     */
    @Schema(description = "名称")
    @TableField("title")
    private String title;

    /**
     * 参数
     */
    @Schema(description = "参数")
    @TableField("properties")
    private String properties;

    /**
     * 必传参数
     */
    @Schema(description = "必传参数")
    @TableField("required")
    private String required;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @TableField("description")
    private String description;
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiSchema apiSchema = (ApiSchema) o;
        return Objects.equals(projectId, apiSchema.projectId) && Objects.equals(type, apiSchema.type) && Objects.equals(
                defKey, apiSchema.defKey) && Objects.equals(title, apiSchema.title) && Objects.equals(properties,
                apiSchema.properties) && Objects.equals(required, apiSchema.required) && Objects.equals(description,
                apiSchema.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(projectId, type, defKey, title, properties, required, description);
    }
}

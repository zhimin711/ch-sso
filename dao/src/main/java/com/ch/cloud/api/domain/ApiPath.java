package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 接口分组对象 bt_api_config
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:35:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "接口分组")
@TableName("bt_api_path")
public class ApiPath extends BaseEntity {

    private String status;
    /**
     * projectId
     */
    @Schema(description = "projectId")
    @TableField("project_id")
    private Long projectId;
    
    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    @TableField("name")
    private String name;
    
    /**
     * 接口地址
     */
    @Schema(description = "接口地址")
    @TableField("path")
    private String path;
    
    /**
     * 请求方法
     */
    @Schema(description = "请求方法")
    @TableField("method")
    private String method;
    
    /**
     * 是否封装
     */
    @Schema(description = "是否封装")
    @TableField("is_wrap")
    private Boolean isWrap;
    
    /**
     * 请求头中 Content-Type 值
     */
    @Schema(description = "请求头中 Content-Type 值")
    @TableField("consumes")
    private String consumes;
    
    /**
     * 请求头中 Accept 值
     */
    @Schema(description = "请求头中 Accept 值")
    @TableField("produces")
    private String produces;
    
    /**
     * 请求参数:json
     */
    @Schema(description = "请求参数")
    @TableField("parameters")
    private String parameters;
    
    @TableField("body")
    private String body;
    /**
     * 返回参数:json
     */
    @Schema(description = "返回参数")
    @TableField("responses")
    private String responses;
    
    /**
     * 描述
     */
    @Schema(description = "描述")
    @TableField("description")
    private String description;
    
    @TableField("is_import")
    private Boolean isImport;
    
    @TableField("import_at")
    private Date importAt;
    
    @TableField("import_content")
    private String importContent;
}

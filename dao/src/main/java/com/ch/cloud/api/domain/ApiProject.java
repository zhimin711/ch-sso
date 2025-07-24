package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ch.cloud.api.dto.ResultWrapDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务-接口项目配置对象 bt_api_project_config
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:34:06
 */
@Data
@Schema(description = "业务-接口项目配置")
@TableName(value = "bt_api_project", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ApiProject extends BaseEntity {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    @TableField("project_id")
    private Long projectId;

    /**
     * 租户空间ID
     */
    @Schema(description = "租户空间ID")
    @TableField("workspace_id")
    private Long workspaceId;

    /**
     * 别名
     */
    @Schema(description = "别名")
    @TableField("name")
    private String name;

    /**
     * api doc url
     */
    @Schema(description = "api doc url")
    @TableField("api_doc_url")
    private String apiDocUrl;

    @Schema(description = "api doc type")
    @TableField("api_doc_type")
    private String apiDocType;

    /**
     * 请求前缀
     */
    @Schema(description = "请求前缀")
    @TableField("base_path")
    private String basePath;

    /**
     * 图标
     */
    @Schema(description = "图标")
    @TableField("icon")
    private String icon;

    /**
     * 是否开放
     */
    @Schema(description = "是否开放")
    @TableField("is_open")
    private Boolean isOpen;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @TableField("description")
    private String description;

    /**
     * 环境配置JSON格式
     */
    @Schema(description = "环境配置JSON格式")
    @TableField("env")
    private String env;

    @Schema(description = "全局配置")
    @TableField("global_config")
    private String globalConfig;

    @Schema(description = "请求结果全局封装")
    @TableField(value = "result_wrap", typeHandler = JacksonTypeHandler.class)
    private ResultWrapDTO resultWrap;
}

package com.ch.cloud.api.dto;

import com.ch.cloud.api.enums.APIDocType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * 业务-接口项目配置对象 bt_api_project_config
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:34:06
 */
@Data
@Schema(description = "业务-接口项目配置")
public class ApiProjectDTO {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 负责人
     */
    @Schema(description = "负责人")
    private String owner;

    /**
     * 别名
     */
    @Schema(description = "别名")
    private String name;

    /**
     * api doc url
     */
    @Schema(description = "api doc url")
    private String apiDocUrl;

    /**
     * 描述
     */
    @Schema(description = "导入文档类型：openapi-doc,swagger_2")
    private APIDocType apiDocType;

    /**
     * 请求前缀
     */
    @Schema(description = "项目API请求前缀")
    private String basePath;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 是否开放
     */
    @Schema(description = "是否开放")
    private Boolean isOpen;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 环境配置JSON格式
     */
    @Schema(description = "环境配置JSON格式")
    private Object env;

    @Schema(description = "请求结果全局封装")
    private ResultWrapDTO resultWrap;
}

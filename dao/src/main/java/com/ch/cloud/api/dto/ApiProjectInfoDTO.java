package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;


/**
 * 业务-接口项目配置对象 bt_api_project_config
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:34:06
 */
@Data
@Schema(description = "业务-接口项目配置")
public class ApiProjectInfoDTO {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 别名
     */
    @Schema(description = "别名")
    private String name;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;


    @Schema(description = "负责人")
    private String owner;
    
    @Schema(description = "项目进度")
    private String progress;
    
    @Schema(description = "更新时间")
    private Date updateAt;
}

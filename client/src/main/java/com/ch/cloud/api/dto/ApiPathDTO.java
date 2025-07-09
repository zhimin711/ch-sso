package com.ch.cloud.api.dto;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/11/15
 */
@Data
@Schema(description = "接口配置信息")
public class ApiPathDTO {
    
    @Schema(description = "接口ID")
    private Long id;
    
    @Schema(description = "项目ID")
    private Long projectId;
    
    @Schema(description = "接口名称")
    private String name;
    
    @Schema(description = "接口地址")
    private String path;
    
    @Schema(description = "请求方法")
    private String method;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "创建时间")
    private Date createAt;
    
    @Schema(description = "更新时间")
    private Date updateAt;
    
    private boolean isWrap = true;

    @Schema(description = "参数")
    private JSONObject parameters;

    @Schema(description = "响应")
    private JSONObject responses;

    @Schema(description = "请求体")
    private JSONObject requestBody;
}

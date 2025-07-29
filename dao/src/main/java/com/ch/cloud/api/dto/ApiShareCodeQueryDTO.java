package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 接口分享码查询DTO
 * 
 * @author zhimin.ma
 * @since 2025-07-28
 */
@Data
@Schema(description = "接口分享码查询参数")
public class ApiShareCodeQueryDTO {
    
    @Schema(description = "项目ID")
    private Long projectId;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "分享码")
    private String shareCode;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
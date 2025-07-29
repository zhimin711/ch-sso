package com.ch.cloud.api.dto;

import com.ch.cloud.api.domain.ApiShareCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 接口分享码响应VO
 * 
 * @author zhimin.ma
 * @since 2025-07-28
 */
@Data
@Schema(description = "接口分享码响应信息")
public class ApiShareCodeVO {
    
    @Schema(description = "分享码ID")
    private Long id;
    
    @Schema(description = "项目ID")
    private Long projectId;
    
    @Schema(description = "分享码")
    private String shareCode;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "资源信息")
    private List<ApiResourceDTO> resources;
    
    @Schema(description = "过期时间")
    private Date expireTime;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    /**
     * 从ApiShareCode转换为ApiShareCodeVO
     * 
     * @param shareCode ApiShareCode实体
     * @return ApiShareCodeVO
     */
    public static ApiShareCodeVO from(ApiShareCode shareCode) {
        if (shareCode == null) {
            return null;
        }
        ApiShareCodeVO vo = new ApiShareCodeVO();
        vo.setId(shareCode.getId());
        vo.setProjectId(shareCode.getProjectId());
        vo.setShareCode(shareCode.getShareCode());
        vo.setUserId(shareCode.getUserId());
        vo.setResources(shareCode.getResources());
        vo.setExpireTime(shareCode.getExpireTime());
        vo.setCreateTime(shareCode.getCreateTime());
        vo.setUpdateTime(shareCode.getUpdateTime());
        return vo;
    }
}
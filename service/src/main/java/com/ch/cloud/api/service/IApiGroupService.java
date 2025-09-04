package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.dto.ApiGroupPathDTO;

import java.util.List;

/**
 * 接口分组业务服务
 * 
 * @author zhimin.ma
 * @since 2022-10-16 11:31:22
 */
public interface IApiGroupService extends IService<ApiGroup> {
    
    ApiGroup getByPathId(Long pathId);
    
    int addGroupPath(Long groupId, Long pathId);
    
    int removeGroupPath(Long groupId, Long pathId);
    
    List<Long> listPathIdsByGroupId(Long id);
    
    List<ApiGroup> listMyGroupByProjectId(Long projectId, String userId);
    
    List<ApiGroup> listApiGroupByProjectId(Long projectId);
    
    List<ApiGroup> listTagGroupByProjectId(Long projectId);
    
    boolean existsGroupPath(Long groupId, Long pathId);
    
    List<ApiGroupPathDTO> listPathIdsByGroupIds(List<Long> groupIds);
}

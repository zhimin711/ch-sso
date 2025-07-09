package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.dto.ApiGroupPathDTO;
import com.ch.cloud.api.enums.GroupType;
import com.ch.cloud.api.mapper.ApiGroupMapper;
import com.ch.cloud.api.mapper2.IApiGroupPathMapper;
import com.ch.cloud.api.service.IApiGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 接口分组Service业务层
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:31:22
 */
@Service
public class ApiGroupServiceImpl extends ServiceImpl<ApiGroupMapper, ApiGroup> implements IApiGroupService {
    
    @Autowired
    private IApiGroupPathMapper apiGroupPathMapper;
    
    
    @Override
    public ApiGroup getByPathId(Long pathId) {
        List<Long> groupIds = apiGroupPathMapper.listGroupIdsByPathIdAndGroupType(pathId, GroupType.MODULE.getCode());
        if (groupIds.isEmpty()) {
            return null;
        }
        return getById(groupIds.get(0));
    }
    
    @Override
    public int addGroupPath(Long groupId, Long apiId) {
        Integer sort = apiGroupPathMapper.maxSortByGroupId(groupId);
        return apiGroupPathMapper.insert(groupId, apiId, sort == null ? 1 : sort + 1);
    }
    
    @Override
    public int removeGroupPath(Long groupId, Long apiId) {
        return apiGroupPathMapper.remove(groupId, apiId);
    }
    
    @Override
    public List<Long> listPathIdsByGroupId(Long groupId) {
        return apiGroupPathMapper.listPathIds(groupId);
    }
    
    @Override
    public List<ApiGroup> listMyGroupByProjectId(Long projectId, String userId) {
        return lambdaQuery().eq(ApiGroup::getProjectId, projectId).eq(ApiGroup::getType, GroupType.CUSTOM.getCode())
                .orderByAsc(ApiGroup::getSort, ApiGroup::getId).list();
    }
    
    @Override
    public List<ApiGroup> listApiGroupByProjectId(Long projectId) {
        return lambdaQuery().eq(ApiGroup::getProjectId, projectId).eq(ApiGroup::getType, GroupType.MODULE.getCode())
                .orderByAsc(ApiGroup::getSort, ApiGroup::getId).list();
    }
    
    @Override
    public List<ApiGroup> listTagGroupByProjectId(Long projectId) {
        return lambdaQuery().eq(ApiGroup::getProjectId, projectId).eq(ApiGroup::getType, GroupType.TAG.getCode())
                .orderByAsc(ApiGroup::getSort, ApiGroup::getId).list();
    }
    
    @Override
    public boolean existsGroupPath(Long groupId, Long pathId) {
        return apiGroupPathMapper.count(groupId, pathId) > 0;
    }
    
    @Override
    public List<ApiGroupPathDTO> listPathIdsByGroupIds(List<Long> groupIds) {
        return apiGroupPathMapper.listPathIdsByGroupIds(groupIds);
    }
}

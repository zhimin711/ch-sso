package com.ch.cloud.api.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.dto.ApiGroupPathDetailDTO;
import com.ch.cloud.api.enums.GroupType;
import com.ch.cloud.api.manager.ApiPathManager;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApiPathManagerImpl implements ApiPathManager {
    
    @Autowired
    private IApiPathService apiConfigService;
    
    @Autowired
    private IApiGroupService apiGroupService;
    
    @Autowired
    private IApiPathService apiPathService;
    
    @Override
    public InvokerPage.Page<ApiPath> pageByGroupId(Long groupId, int pageNum, int pageSize) {
        apiConfigService.pageByGroupId(groupId, pageNum, pageSize);
        return null;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean save(ApiGroupPathDetailDTO record) {
        
        ApiPath path = convertPath(record);
        List<ApiPath> list = apiPathService.lambdaQuery().eq(ApiPath::getPath, path.getPath())
                .eq(ApiPath::getMethod, path.getMethod()).eq(ApiPath::getProjectId, path.getProjectId()).list();
        Assert.isEmpty(list, PubError.EXISTS, "路径" + path.getPath(), path.getMethod());
        apiPathService.save(path);
        
        List<ApiGroup> groupList = apiGroupService.lambdaQuery().eq(ApiGroup::getProjectId, record.getProjectId())
                .eq(ApiGroup::getId, record.getGroupId()).eq(ApiGroup::getType, GroupType.MODULE.getCode()).list();
        Assert.notEmpty(groupList, PubError.NOT_EXISTS, "分组", record.getGroupId());
        apiGroupService.addGroupPath(record.getGroupId(), path.getId());
        if (CommonUtils.isNotEmpty(record.getTagGroupIds())) {
            record.getTagGroupIds().forEach(groupId -> {
                apiGroupService.addGroupPath(groupId, path.getId());
            });
        }
        if (CommonUtils.isNotEmpty(record.getCustomGroupIds())) {
            record.getCustomGroupIds().forEach(groupId -> {
                apiGroupService.addGroupPath(groupId, path.getId());
            });
        }
        return true;
    }
    
    private ApiPath convertPath(ApiGroupPathDetailDTO record) {
        ApiPath path = BeanUtil.copyProperties(record, ApiPath.class);
        if (CommonUtils.isEmpty(record.getMethod())) {
            path.setMethod(record.getMethod().toLowerCase());
        }
        if (CommonUtils.isNotEmpty(record.getParameters())) {
            path.setParameters(record.getParameters().toJSONString());
        }
        if (CommonUtils.isNotEmpty(record.getResponses())) {
            path.setResponses(record.getResponses().toJSONString());
        }
        if (CommonUtils.isNotEmpty(record.getRequestBody())) {
            path.setBody(record.getRequestBody().toJSONString());
        }
        return path;
    }
    
    @Override
    public List<Long> listGroupIds(Long pathId) {
        return apiPathService.listGroupIds(pathId);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ApiGroupPathDetailDTO record) {
        ApiPath path = convertPath(record);
        apiPathService.updateById(path);
        List<Long> groupIds = apiPathService.listGroupIds(path.getId());
        List<Long> newGroupIds = Lists.newArrayList();
        List<Long> existsGroupIds = Lists.newArrayList();
        if (!groupIds.contains(record.getGroupId())) {
            newGroupIds.add(record.getGroupId());
        } else {
            existsGroupIds.add(record.getGroupId());
        }
        if (CommonUtils.isNotEmpty(record.getTagGroupIds())) {
            record.getTagGroupIds().forEach(groupId -> {
                if (!groupIds.contains(groupId)) {
                    newGroupIds.add(groupId);
                } else {
                    existsGroupIds.add(groupId);
                }
            });
        }
        if (CommonUtils.isNotEmpty(record.getCustomGroupIds())) {
            record.getCustomGroupIds().forEach(groupId -> {
                if (!groupIds.contains(groupId)) {
                    newGroupIds.add(groupId);
                } else {
                    existsGroupIds.add(groupId);
                }
            });
        }
        // 新增 新添加接口分组
        if (CommonUtils.isNotEmpty(newGroupIds)) {
            record.getTagGroupIds().forEach(groupId -> {
                apiGroupService.addGroupPath(groupId, path.getId());
            });
        }
        // 删除 移除的接口分组
        if (CommonUtils.isNotEmpty(existsGroupIds)) {
            groupIds.stream().filter(groupId -> !existsGroupIds.contains(groupId))
                    .forEach(groupId -> apiGroupService.removeGroupPath(groupId, path.getId()));
        }
        return true;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(Long id) {
        boolean ok = apiPathService.removeById(id);
        if (ok) {
            List<Long> groupIds = apiPathService.listGroupIds(id);
            if (CommonUtils.isNotEmpty(groupIds)) {
                groupIds.forEach(groupId -> apiGroupService.removeGroupPath(groupId, id));
            }
        }
        return ok;
    }
}

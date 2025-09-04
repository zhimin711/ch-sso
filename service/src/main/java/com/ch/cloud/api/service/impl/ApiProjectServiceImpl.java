package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.dto.ApiProjectRoleDTO;
import com.ch.cloud.api.mapper.ApiProjectMapper;
import com.ch.cloud.api.mapper2.IApiProjectUserMapper;
import com.ch.cloud.api.service.IApiProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * decs:
 *
 * @author Zhimin.Ma
 * @date 2019/11/5
 */
@Service
@Slf4j
public class ApiProjectServiceImpl extends ServiceImpl<ApiProjectMapper, ApiProject>
        implements IApiProjectService {
    
    @Resource
    private IApiProjectUserMapper apiProjectUserMapper;
    
    @Override
    public List<Long> findByUserId(String userId) {
        return apiProjectUserMapper.findProjectIdsByUserId(userId);
    }
    
    @Override
    public ApiProject getByProjectId(Long projectId) {
        if (projectId == null) {
            return null;
        }
        return lambdaQuery().eq(ApiProject::getProjectId, projectId).one();
    }
    
    @Override
    public ApiProjectRoleDTO findByUserIdAndProjectId(String userId, Long projectId) {
        return apiProjectUserMapper.findByUserIdAndProjectId(userId,projectId);
    }
    
//    @Override
    public boolean insert(ApiProject apiProject){
        return getBaseMapper().insert(apiProject)>0;
    }
}

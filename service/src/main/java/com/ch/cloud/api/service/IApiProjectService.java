package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.dto.ApiProjectRoleDTO;

import java.util.List;

/**
 * decs:
 *
 * @author Zhimin.Ma
 * @since 2019/11/5
 */
public interface IApiProjectService extends IService<ApiProject> {

    List<Long> findByUserId(String userId);
    
    ApiProject getByProjectId(Long projectId);
    
    ApiProjectRoleDTO findByUserIdAndProjectId(String userId, Long projectId);
}

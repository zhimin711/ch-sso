package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;
import com.ch.cloud.api.dto.EnvDTO;

import java.util.List;

/**
 * 租户环境配置服务接口
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
public interface IApiTenantService extends IService<ApiTenant> {

    /**
     * 根据租户空间ID获取租户环境配置
     *
     * @param workspaceId 租户空间ID
     * @return 租户环境配置
     */
    ApiTenant getByWorkspaceId(Long workspaceId);

    /**
     * 保存或更新租户环境配置
     *
     * @param dto 租户环境配置DTO
     * @return 是否成功
     */
    Boolean saveOrUpdateConfig(ApiTenantDTO dto);
    
    List<EnvDTO> getEnvironments(Long tenantId);
}
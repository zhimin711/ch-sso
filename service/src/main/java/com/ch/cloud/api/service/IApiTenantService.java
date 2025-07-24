package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;

import java.util.List;

/**
 * 租户环境配置服务接口
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
public interface IApiTenantService extends IService<ApiTenant> {

    /**
     * 根据租户空间ID获取环境配置列表
     *
     * @param workspaceId 租户空间ID
     * @return 环境配置列表
     */
    List<ApiTenant> getByWorkspaceId(Long workspaceId);

    /**
     * 根据租户空间ID和环境标识获取环境配置
     *
     * @param workspaceId 租户空间ID
     * @param envKey 环境标识
     * @return 环境配置
     */
    ApiTenant getByWorkspaceIdAndEnvKey(Long workspaceId, String envKey);

    /**
     * 根据租户空间ID获取默认环境配置
     *
     * @param workspaceId 租户空间ID
     * @return 默认环境配置
     */
    ApiTenant getDefaultByWorkspaceId(Long workspaceId);

    /**
     * 保存或更新环境配置
     *
     * @param dto 环境配置DTO
     * @return 是否成功
     */
    Boolean saveOrUpdateConfig(ApiTenantDTO dto);

    /**
     * 删除环境配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    Boolean deleteConfig(Long id);
} 
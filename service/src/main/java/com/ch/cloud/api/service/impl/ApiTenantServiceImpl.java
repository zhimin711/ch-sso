package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;
import com.ch.cloud.api.dto.EnvDTO;
import com.ch.cloud.api.mapper.ApiTenantMapper;
import com.ch.cloud.api.service.IApiTenantService;
import com.ch.cloud.api.utils.TenantConfigUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 租户环境配置服务实现类
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@Service
public class ApiTenantServiceImpl extends ServiceImpl<ApiTenantMapper, ApiTenant> implements IApiTenantService {

    @Override
    public ApiTenant getByWorkspaceId(Long workspaceId) {
        LambdaQueryWrapper<ApiTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTenant::getWorkspaceId, workspaceId)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateConfig(ApiTenantDTO dto) {
        ApiTenant entity = new ApiTenant();
        BeanUtils.copyProperties(dto, entity);

        // 如果没有环境配置，创建默认配置
        if (entity.getEnv() == null) {
            entity.setEnv(TenantConfigUtils.createDefaultEnvironments());
        }

        if (dto.getId() != null) {
            // 更新
            return this.updateById(entity);
        } else {
            // 新增
            return this.save(entity);
        }
    }

    /**
     * 获取租户的所有环境配置
     *
     * @param workspaceId 租户空间ID
     * @return 环境配置列表
     */
    @Override
    public List<EnvDTO> getEnvironments(Long workspaceId) {
        ApiTenant tenant = getByWorkspaceId(workspaceId);
        if (tenant == null) {
            return Lists.newArrayList();
        }

        return TenantConfigUtils.getAllEnvironments(tenant.getEnv());
    }

} 
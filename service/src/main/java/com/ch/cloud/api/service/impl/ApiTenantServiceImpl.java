package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;
import com.ch.cloud.api.mapper.ApiTenantMapper;
import com.ch.cloud.api.service.IApiTenantService;

import com.ch.e.Assert;
import com.ch.e.PubError;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 租户环境配置服务实现
 *
 * @author zhimin.ma
 * @since 2025-07-28
 */
@Service
public class ApiTenantServiceImpl extends ServiceImpl<ApiTenantMapper, ApiTenant> implements IApiTenantService {
    @Override
    public ApiTenant getByWorkspaceId(Long tenantId) {
        return lambdaQuery().eq(ApiTenant::getTenantId, tenantId).one();
    }

    @Override
    public Boolean saveOrUpdateConfig(ApiTenantDTO dto) {
        ApiTenant tenant = this.getByWorkspaceId(dto.getTenantId());
        Assert.notNull(tenant, PubError.NOT_EXISTS, dto.getTenantId(), "租户不存在");
        BeanUtils.copyProperties(dto, tenant);
        return saveOrUpdate(tenant);
    }
}

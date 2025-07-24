package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;
import com.ch.cloud.api.mapper.ApiTenantMapper;
import com.ch.cloud.api.service.IApiTenantService;
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
    public List<ApiTenant> getByWorkspaceId(Long workspaceId) {
        LambdaQueryWrapper<ApiTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTenant::getWorkspaceId, workspaceId)
                .eq(ApiTenant::getStatus, "1")
                .orderByAsc(ApiTenant::getIsDefault)
                .orderByAsc(ApiTenant::getEnvKey);
        return this.list(wrapper);
    }

    @Override
    public ApiTenant getByWorkspaceIdAndEnvKey(Long workspaceId, String envKey) {
        LambdaQueryWrapper<ApiTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTenant::getWorkspaceId, workspaceId)
                .eq(ApiTenant::getEnvKey, envKey)
                .eq(ApiTenant::getStatus, "1");
        return this.getOne(wrapper);
    }

    @Override
    public ApiTenant getDefaultByWorkspaceId(Long workspaceId) {
        LambdaQueryWrapper<ApiTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTenant::getWorkspaceId, workspaceId)
                .eq(ApiTenant::getIsDefault, true)
                .eq(ApiTenant::getStatus, "1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateConfig(ApiTenantDTO dto) {
        ApiTenant entity = new ApiTenant();
        BeanUtils.copyProperties(dto, entity);

        // 如果设置为默认环境，需要将其他环境设置为非默认
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            LambdaUpdateWrapper<ApiTenant> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ApiTenant::getWorkspaceId, dto.getWorkspaceId())
                    .set(ApiTenant::getIsDefault, false);
            this.update(updateWrapper);
        }

        if (dto.getId() != null) {
            // 更新
            return this.updateById(entity);
        } else {
            // 新增
            return this.save(entity);
        }
    }

    @Override
    public Boolean deleteConfig(Long id) {
        return this.removeById(id);
    }
} 
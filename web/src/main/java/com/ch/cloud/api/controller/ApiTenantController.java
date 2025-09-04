package com.ch.cloud.api.controller;

import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;
import com.ch.cloud.api.dto.EnvDTO;
import com.ch.cloud.api.service.IApiTenantService;
import com.ch.cloud.upms.client.UpmsTenantClient;
import com.ch.core.result.Result;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.toolkit.ContextUtil;
import com.google.common.collect.Lists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户环境配置管理Controller
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/tenant")
@Tag(name = "api-tenant-controller", description = "租户环境配置管理")
public class ApiTenantController {

    @Autowired
    private IApiTenantService apiTenantService;

    @Autowired
    private UpmsTenantClient upmsTenantClient;

    // ==================== 租户环境配置管理 ====================

    // @Operation(summary = "获取租户环境配置", description = "根据租户空间ID获取环境配置")
    // @GetMapping("/{tenantId}")
    // public Result<ApiTenant> getTenantConfig(@PathVariable Long tenantId) {
    //     return Result.from(() -> apiTenantService.getByWorkspaceId(tenantId));
    // }


    @Operation(summary = "获取租户所有环境配置", description = "根据租户空间ID获取所有环境配置")
    @GetMapping("/{tenantId}/env")
    public Result<List<EnvDTO>> getEnvironments(@PathVariable Long tenantId) {
        return Result.from(() -> {
            com.ch.result.Result<Boolean> adminResult = upmsTenantClient.hasAdmin(tenantId, ContextUtil.getUsername());
            Assert.isTrue(adminResult.get(), PubError.NOT_ALLOWED, tenantId, "租户空间");
            ApiTenant tenant = apiTenantService.lambdaQuery().eq(ApiTenant::getTenantId, tenantId).one();
            if (tenant == null) {
                return Lists.newArrayList();
            }
            return tenant.getEnvList();
        });
    }


    @Operation(summary = "保存或更新租户配置", description = "保存或更新租户配置")
    @PostMapping("/save")
    public Result<Boolean> saveTenantConfig(@RequestBody ApiTenantDTO dto) {
        return Result.from(() ->{

            com.ch.result.Result<Boolean> adminResult = upmsTenantClient.hasAdmin(dto.getTenantId(), ContextUtil.getUsername());
            Assert.isTrue(adminResult.get(), PubError.NOT_ALLOWED, dto.getTenantId(), "租户空间");
            return apiTenantService.saveOrUpdateConfig(dto);
        });
    }

}

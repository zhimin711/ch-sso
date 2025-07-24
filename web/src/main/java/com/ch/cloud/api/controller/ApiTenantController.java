package com.ch.cloud.api.controller;

import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.ApiTenantDTO;
import com.ch.cloud.api.dto.MergedEnvConfigDTO;
import com.ch.cloud.api.service.IApiTenantService;
import com.ch.cloud.api.service.ApiTenantManager;
import com.ch.core.result.Result;
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
    private ApiTenantManager apiTenantManager;

    // ==================== 租户环境配置管理 ====================

    @Operation(summary = "获取租户环境配置列表", description = "根据租户空间ID获取环境配置列表")
    @GetMapping("/list/{workspaceId}")
    public Result<List<ApiTenant>> getTenantEnvConfigList(@PathVariable Long workspaceId) {
        return Result.from(() -> apiTenantService.getByWorkspaceId(workspaceId));
    }

    @Operation(summary = "获取租户环境配置详情", description = "根据租户空间ID和环境标识获取环境配置详情")
    @GetMapping("/detail/{workspaceId}/{envKey}")
    public Result<ApiTenant> getTenantEnvConfigDetail(@PathVariable Long workspaceId, @PathVariable String envKey) {
        return Result.from(() -> apiTenantService.getByWorkspaceIdAndEnvKey(workspaceId, envKey));
    }

    @Operation(summary = "获取租户默认环境配置", description = "获取租户的默认环境配置")
    @GetMapping("/default/{workspaceId}")
    public Result<ApiTenant> getTenantDefaultEnvConfig(@PathVariable Long workspaceId) {
        return Result.from(() -> apiTenantService.getDefaultByWorkspaceId(workspaceId));
    }

    @Operation(summary = "保存或更新租户环境配置", description = "保存或更新租户环境配置")
    @PostMapping("/save")
    public Result<Boolean> saveTenantEnvConfig(@RequestBody ApiTenantDTO dto) {
        return Result.from(() -> apiTenantService.saveOrUpdateConfig(dto));
    }

    @Operation(summary = "删除租户环境配置", description = "删除指定的租户环境配置")
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteTenantEnvConfig(@PathVariable Long id) {
        return Result.from(() -> apiTenantService.deleteConfig(id));
    }

    // ==================== 项目环境配置管理 ====================

    @Operation(summary = "获取项目环境配置", description = "获取项目的环境配置列表")
    @GetMapping("/project/{projectId}")
    public Result<List<MergedEnvConfigDTO>> getProjectEnvConfigs(@PathVariable Long projectId) {
        return Result.from(() -> apiTenantManager.getProjectEnvConfigs(projectId));
    }

    @Operation(summary = "获取项目特定环境配置", description = "获取项目特定环境标识的配置")
    @GetMapping("/project/{projectId}/{envKey}")
    public Result<MergedEnvConfigDTO> getProjectEnvConfig(@PathVariable Long projectId, @PathVariable String envKey) {
        return Result.from(() -> apiTenantManager.getProjectEnvConfig(projectId, envKey));
    }

    @Operation(summary = "检查项目是否有自定义配置", description = "检查项目是否有自定义环境配置")
    @GetMapping("/project/{projectId}/has-custom")
    public Result<Boolean> hasProjectCustomConfig(@PathVariable Long projectId) {
        return Result.from(() -> apiTenantManager.hasProjectCustomConfig(projectId));
    }

    // ==================== 工具接口 ====================

    @Operation(summary = "获取租户配置的项目格式", description = "将租户环境配置转换为项目env字段格式")
    @GetMapping("/{workspaceId}/project-format")
    public Result<String> getTenantConfigAsProjectFormat(@PathVariable Long workspaceId) {
        return Result.from(() -> apiTenantManager.getTenantConfigAsProjectEnvFormat(workspaceId));
    }

    @Operation(summary = "获取项目配置的旧格式", description = "获取项目环境配置的旧格式JSON字符串")
    @GetMapping("/project/{projectId}/legacy-format")
    public Result<String> getProjectConfigAsLegacyFormat(@PathVariable Long projectId) {
        return Result.from(() -> apiTenantManager.getProjectEnvConfigAsLegacyFormat(projectId));
    }
} 
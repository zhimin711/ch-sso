package com.ch.cloud.api.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.EnvDTO;
import com.ch.cloud.api.dto.MergedEnvConfigDTO;
import com.ch.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户环境配置管理工具类
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@Component
public class ApiTenantManager {

    @Autowired
    private IApiTenantService apiTenantService;

    @Autowired
    private IApiProjectService apiProjectService;

    /**
     * 获取项目的环境配置
     * 优先使用项目env字段配置，如果没有则使用租户默认配置
     *
     * @param projectId 项目ID
     * @return 合并后的环境配置列表
     */
    public List<MergedEnvConfigDTO> getProjectEnvConfigs(Long projectId) {
        // 获取项目信息
        ApiProject project = apiProjectService.getByProjectId(projectId);
        if (project == null) {
            return null;
        }

        List<MergedEnvConfigDTO> mergedConfigs = new ArrayList<>();

        // 优先使用项目env字段配置
        if (CommonUtils.isNotEmpty(project.getEnv()) && JSON.isValidObject(project.getEnv())) {
            JSONObject envJson = JSON.parseObject(project.getEnv());
            if (envJson.containsKey("envList")) {
                List<EnvDTO> envList = envJson.getList("envList", EnvDTO.class);
                for (EnvDTO env : envList) {
                    MergedEnvConfigDTO mergedConfig = new MergedEnvConfigDTO();
                    mergedConfig.setEnvKey(env.getName() != null ? env.getName().toLowerCase() : "default");
                    mergedConfig.setName(env.getName());
                    mergedConfig.setDomain(env.getDomain());
                    mergedConfig.setPrefix(env.getPrefix());
                    mergedConfig.setConfigSource("project");
                    mergedConfigs.add(mergedConfig);
                }
                return mergedConfigs;
            }
        }

        // 如果项目没有配置，则使用租户默认配置
        if (project.getWorkspaceId() != null) {
            List<ApiTenant> tenantConfigs = apiTenantService.getByWorkspaceId(project.getWorkspaceId());
            for (ApiTenant tenantConfig : tenantConfigs) {
                MergedEnvConfigDTO mergedConfig = new MergedEnvConfigDTO();
                mergedConfig.setEnvKey(tenantConfig.getEnvKey());
                mergedConfig.setName(tenantConfig.getName());
                mergedConfig.setDomain(tenantConfig.getDomain());
                mergedConfig.setPrefix(tenantConfig.getPrefix());
                mergedConfig.setDescription(tenantConfig.getDescription());
                mergedConfig.setEnvConfig(tenantConfig.getEnvConfig());
                mergedConfig.setConfigSource("tenant");
                mergedConfig.setTenantConfigId(tenantConfig.getId());
                mergedConfigs.add(mergedConfig);
            }
        }

        return mergedConfigs;
    }

    /**
     * 获取项目特定环境标识的配置
     *
     * @param projectId 项目ID
     * @param envKey 环境标识
     * @return 合并后的环境配置
     */
    public MergedEnvConfigDTO getProjectEnvConfig(Long projectId, String envKey) {
        List<MergedEnvConfigDTO> configs = getProjectEnvConfigs(projectId);
        if (configs != null) {
            return configs.stream()
                    .filter(config -> envKey.equals(config.getEnvKey()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 获取租户的环境配置列表
     *
     * @param workspaceId 租户空间ID
     * @return 租户环境配置列表
     */
    public List<ApiTenant> getTenantEnvConfigs(Long workspaceId) {
        return apiTenantService.getByWorkspaceId(workspaceId);
    }

    /**
     * 获取租户默认环境配置
     *
     * @param workspaceId 租户空间ID
     * @return 租户默认环境配置
     */
    public ApiTenant getTenantDefaultEnvConfig(Long workspaceId) {
        return apiTenantService.getDefaultByWorkspaceId(workspaceId);
    }

    /**
     * 检查项目是否有自定义环境配置
     *
     * @param projectId 项目ID
     * @return 是否有自定义配置
     */
    public boolean hasProjectCustomConfig(Long projectId) {
        ApiProject project = apiProjectService.getByProjectId(projectId);
        if (project == null || CommonUtils.isEmpty(project.getEnv())) {
            return false;
        }
        return JSON.isValidObject(project.getEnv());
    }

    /**
     * 获取项目的环境配置（兼容旧版本）
     * 将新的环境配置格式转换为旧的env字段格式
     *
     * @param projectId 项目ID
     * @return 旧格式的环境配置JSON字符串
     */
    public String getProjectEnvConfigAsLegacyFormat(Long projectId) {
        List<MergedEnvConfigDTO> configs = getProjectEnvConfigs(projectId);
        if (configs == null || configs.isEmpty()) {
            return null;
        }

        // 转换为旧格式的JSON字符串
        StringBuilder envJson = new StringBuilder();
        envJson.append("{\"envList\":[");
        
        for (int i = 0; i < configs.size(); i++) {
            MergedEnvConfigDTO config = configs.get(i);
            envJson.append("{");
            envJson.append("\"id\":").append(i + 1).append(",");
            envJson.append("\"name\":\"").append(config.getName()).append("\",");
            envJson.append("\"domain\":\"").append(config.getDomain()).append("\",");
            envJson.append("\"prefix\":\"").append(config.getPrefix() != null ? config.getPrefix() : "").append("\"");
            envJson.append("}");
            if (i < configs.size() - 1) {
                envJson.append(",");
            }
        }
        
        envJson.append("]}");
        return envJson.toString();
    }

    /**
     * 将租户环境配置转换为项目env字段格式
     *
     * @param workspaceId 租户空间ID
     * @return 项目env字段格式的JSON字符串
     */
    public String getTenantConfigAsProjectEnvFormat(Long workspaceId) {
        List<ApiTenant> tenantConfigs = apiTenantService.getByWorkspaceId(workspaceId);
        if (tenantConfigs == null || tenantConfigs.isEmpty()) {
            return null;
        }

        StringBuilder envJson = new StringBuilder();
        envJson.append("{\"envList\":[");
        
        for (int i = 0; i < tenantConfigs.size(); i++) {
            ApiTenant config = tenantConfigs.get(i);
            envJson.append("{");
            envJson.append("\"id\":").append(i + 1).append(",");
            envJson.append("\"name\":\"").append(config.getName()).append("\",");
            envJson.append("\"domain\":\"").append(config.getDomain()).append("\",");
            envJson.append("\"prefix\":\"").append(config.getPrefix() != null ? config.getPrefix() : "").append("\"");
            envJson.append("}");
            if (i < tenantConfigs.size() - 1) {
                envJson.append(",");
            }
        }
        
        envJson.append("]}");
        return envJson.toString();
    }
} 
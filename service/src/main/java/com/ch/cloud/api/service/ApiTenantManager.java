package com.ch.cloud.api.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.domain.ApiTenant;
import com.ch.cloud.api.dto.EnvDTO;
import com.ch.cloud.api.utils.TenantConfigUtils;
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

    /**
     * 获取租户的环境配置列表
     *
     * @param workspaceId 租户空间ID
     * @return 租户环境配置列表
     */
    public List<EnvDTO> getTenantEnvironments(Long workspaceId) {
        ApiTenant tenant = apiTenantService.getByWorkspaceId(workspaceId);
        if (tenant == null) {
            return TenantConfigUtils.createDefaultEnvironments();
        }
        return TenantConfigUtils.getAllEnvironments(tenant.getEnv());
    }


    /**
     * 将租户环境配置转换为项目env字段格式（兼容旧版本）
     *
     * @param workspaceId 租户空间ID
     * @return 项目env字段格式的JSON字符串
     */
    public String getTenantConfigAsProjectEnvFormat(Long workspaceId) {
        List<EnvDTO> environments = getTenantEnvironments(workspaceId);
        if (environments == null || environments.isEmpty()) {
            return null;
        }

        StringBuilder envJson = new StringBuilder();
        envJson.append("{\"envList\":[");
        
        for (int i = 0; i < environments.size(); i++) {
            EnvDTO config = environments.get(i);
            envJson.append("{");
            envJson.append("\"id\":").append(i + 1).append(",");
            envJson.append("\"name\":\"").append(config.getName()).append("\",");
            envJson.append("\"domain\":\"").append(config.getDomain()).append("\",");
            envJson.append("\"prefix\":\"").append(config.getPrefix() != null ? config.getPrefix() : "").append("\"");
            envJson.append("}");
            if (i < environments.size() - 1) {
                envJson.append(",");
            }
        }
        
        envJson.append("]}");
        return envJson.toString();
    }

    /**
     * 检查租户是否有环境配置
     *
     * @param workspaceId 租户空间ID
     * @return 是否有环境配置
     */
    public boolean hasTenantEnvironments(Long workspaceId) {
        ApiTenant tenant = apiTenantService.getByWorkspaceId(workspaceId);
        if (tenant == null || CommonUtils.isEmpty(tenant.getEnv())) {
            return false;
        }
        try {
            List<EnvDTO> environments = TenantConfigUtils.parseEnvironments(tenant.getEnv());
            return environments != null && !environments.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取租户环境配置的JSON字符串
     *
     * @param workspaceId 租户空间ID
     * @return 环境配置JSON字符串
     */
    public String getTenantEnvironmentsAsJson(Long workspaceId) {
        List<EnvDTO> environments = getTenantEnvironments(workspaceId);
        if (environments == null || environments.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(environments);
    }
} 
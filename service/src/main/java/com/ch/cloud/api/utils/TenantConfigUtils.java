package com.ch.cloud.api.utils;

import com.alibaba.fastjson2.JSON;
import com.ch.cloud.api.dto.EnvDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户环境配置工具类
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
public class TenantConfigUtils {
    
    /**
     * 创建默认环境配置
     *
     * @return 默认环境配置列表
     */
    public static List<EnvDTO> createDefaultEnvironments() {
        List<EnvDTO> environments = new ArrayList<>();
        
        EnvDTO devEnv = new EnvDTO();
        devEnv.setName("dev");
        devEnv.setDomain("https://dev-api.example.com");
        devEnv.setPrefix("/api/v1");
        devEnv.setDescription("开发环境配置");
        environments.add(devEnv);
        
        EnvDTO testEnv = new EnvDTO();
        testEnv.setName("test");
        testEnv.setDomain("https://test-api.example.com");
        testEnv.setPrefix("/api/v1");
        testEnv.setDescription("测试环境配置");
        environments.add(testEnv);
        
        return environments;
    }

    /**
     * 解析环境配置
     *
     * @param envJson 环境配置JSON
     * @return 环境配置列表
     */
    public static List<EnvDTO> parseEnvironments(Object envJson) {
        if (envJson == null) {
            return createDefaultEnvironments();
        }

        try {
            String jsonStr = JSON.toJSONString(envJson);
            return JSON.parseArray(jsonStr, EnvDTO.class);
        } catch (Exception e) {
            // 解析失败，返回默认配置
            
            return createDefaultEnvironments();
        }
    }

    /**
     * 获取环境配置
     *
     * @param envJson 环境配置JSON
     * @param envKey 环境标识
     * @return 环境配置
     */
    public static EnvDTO getEnvironment(Object envJson, String envKey) {
        List<EnvDTO> environments = parseEnvironments(envJson);
        for (EnvDTO env : environments) {
            if (envKey.equals(env.getName())) {
                return env;
            }
        }
        return null;
    }


    /**
     * 添加或更新环境配置
     *
     * @param envJson 原环境配置JSON
     * @param environment 环境配置
     * @return 更新后的环境配置JSON
     */
    public static Object updateEnvironment(Object envJson, EnvDTO environment) {
        List<EnvDTO> environments = parseEnvironments(envJson);
        
        // 查找并更新或添加环境配置
        boolean found = false;
        for (int i = 0; i < environments.size(); i++) {
            EnvDTO env = environments.get(i);
            if (environment.getName().equals(env.getName())) {
                environments.set(i, environment);
                found = true;
                break;
            }
        }

        if (!found) {
            environments.add(environment);
        }

        return environments;
    }

    /**
     * 删除环境配置
     *
     * @param envJson 原环境配置JSON
     * @param envKey 环境标识
     * @return 更新后的环境配置JSON
     */
    public static Object deleteEnvironment(Object envJson, String envKey) {
        List<EnvDTO> environments = parseEnvironments(envJson);
        environments.removeIf(env -> envKey.equals(env.getName()));
        return environments;
    }

    /**
     * 获取所有环境配置
     *
     * @param envJson 环境配置JSON
     * @return 环境配置列表
     */
    public static List<EnvDTO> getAllEnvironments(Object envJson) {
        return parseEnvironments(envJson);
    }
} 
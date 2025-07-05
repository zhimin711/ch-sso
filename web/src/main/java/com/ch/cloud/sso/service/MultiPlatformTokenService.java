package com.ch.cloud.sso.service;

import com.ch.cloud.sso.biz.tools.TokenCacheTool;
import com.ch.cloud.sso.dto.TokenDTO;
import com.ch.cloud.sso.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 多平台Token管理服务
 * 处理跨平台Token同步和授权码机制
 *
 * @author zhimin
 * @since 2025/1/1
 */
@Slf4j
@Service
public class MultiPlatformTokenService {

    @Autowired
    private TokenCacheTool tokenCacheTool;

    /**
     * 生成跨平台授权码
     *
     * @param username 用户名
     * @param sourcePlatform 源平台
     * @param targetPlatform 目标平台
     * @return 授权码
     */
    public String generateCrossPlatformAuthCode(String username, String sourcePlatform, String targetPlatform) {
        // 验证源平台Token是否存在
        String sourceToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, sourcePlatform);
        if (sourceToken == null) {
            log.warn("源平台Token不存在，无法生成授权码: {}, {}", username, sourcePlatform);
            return null;
        }

        // 生成授权码，有效期5分钟
        return tokenCacheTool.generateAuthCode(username, targetPlatform);
    }

    /**
     * 通过授权码获取目标平台Token
     *
     * @param authCode 授权码
     * @param targetPlatform 目标平台
     * @return Token信息
     */
    public Map<String, Object> getTokenByAuthCode(String authCode, String targetPlatform) {
        try {
            // 验证授权码
            Map<String, Object> authCodeInfo = tokenCacheTool.validateAuthCode(authCode);
            if (authCodeInfo == null) {
                log.warn("授权码无效: {}", authCode);
                return null;
            }

            String username = (String) authCodeInfo.get("username");
            String sourcePlatform = (String) authCodeInfo.get("sourcePlatform");
            String expectedTargetPlatform = (String) authCodeInfo.get("targetPlatform");

            // 验证目标平台是否匹配
            if (!targetPlatform.equals(expectedTargetPlatform)) {
                log.warn("目标平台不匹配: 期望 {}, 实际 {}", expectedTargetPlatform, targetPlatform);
                return null;
            }

            // 检查目标平台是否已有Token
            String existingToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, targetPlatform);
            if (existingToken != null) {
                // 如果已有Token，直接返回
                TokenDTO tokenDTO = tokenCacheTool.getTokenCache(existingToken);
                if (tokenDTO != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("token", existingToken);
                    result.put("userInfo", tokenDTO);
                    result.put("isNew", false);
                    return result;
                }
            }

            // 同步Token到目标平台
            boolean syncSuccess = tokenCacheTool.syncUserTokenToPlatform(username, sourcePlatform, targetPlatform, 3600);
            if (!syncSuccess) {
                log.error("同步Token到目标平台失败: {}, {}, {}", username, sourcePlatform, targetPlatform);
                return null;
            }

            // 获取新生成的Token
            String newToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, targetPlatform);
            TokenDTO newTokenDTO = tokenCacheTool.getTokenCache(newToken);

            // 使用并删除授权码
            tokenCacheTool.consumeAuthCode(authCode);

            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("userInfo", newTokenDTO);
            result.put("isNew", true);

            log.info("通过授权码获取Token成功: {}, 用户: {}, 目标平台: {}", authCode, username, targetPlatform);
            return result;

        } catch (Exception e) {
            log.error("通过授权码获取Token失败: {}", authCode, e);
            return null;
        }
    }

    /**
     * 刷新目标平台Token
     *
     * @param username 用户名
     * @param sourcePlatform 源平台
     * @param targetPlatform 目标平台
     * @return 是否刷新成功
     */
    public boolean refreshTargetPlatformToken(String username, String sourcePlatform, String targetPlatform) {
        try {
            // 检查源平台Token是否有效
            String sourceToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, sourcePlatform);
            if (sourceToken == null || !tokenCacheTool.hasAccessToken(sourceToken)) {
                log.warn("源平台Token无效，无法刷新目标平台Token: {}, {}", username, sourcePlatform);
                return false;
            }

            // 删除目标平台旧Token
            tokenCacheTool.deleteUserPlatformTokens(username, targetPlatform);

            // 同步新Token到目标平台
            boolean syncSuccess = tokenCacheTool.syncUserTokenToPlatform(username, sourcePlatform, targetPlatform, 3600);
            if (syncSuccess) {
                log.info("刷新目标平台Token成功: {}, 源平台: {}, 目标平台: {}", username, sourcePlatform, targetPlatform);
            }
            return syncSuccess;

        } catch (Exception e) {
            log.error("刷新目标平台Token失败: {}, {}, {}", username, sourcePlatform, targetPlatform, e);
            return false;
        }
    }

    /**
     * 验证用户在所有平台的Token状态
     *
     * @param username 用户名
     * @return 平台Token状态映射
     */
    public Map<String, Boolean> validateUserAllPlatformTokens(String username) {
        Map<String, Boolean> platformStatus = new HashMap<>();

        // 检查权限管理平台Token
        String authToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, TokenCacheTool.PLATFORM_AUTH);
        platformStatus.put(TokenCacheTool.PLATFORM_AUTH, authToken != null && tokenCacheTool.hasAccessToken(authToken));

        // 检查接口管理平台Token
        String apiToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, TokenCacheTool.PLATFORM_API);
        platformStatus.put(TokenCacheTool.PLATFORM_API, apiToken != null && tokenCacheTool.hasAccessToken(apiToken));

        return platformStatus;
    }

    /**
     * 获取用户跨平台Token信息
     *
     * @param username 用户名
     * @return 跨平台Token信息
     */
    public Map<String, Object> getUserCrossPlatformInfo(String username) {
        Map<String, Object> result = new HashMap<>();

        // 获取所有平台Token
        Map<String, String> platformTokens = tokenCacheTool.getUserAllPlatformTokens(username);
        result.put("platformTokens", platformTokens);

        // 获取Token状态
        Map<String, Boolean> platformStatus = validateUserAllPlatformTokens(username);
        result.put("platformStatus", platformStatus);

        // 获取用户基本信息（从权限管理平台获取）
        String authToken = platformTokens.get(TokenCacheTool.PLATFORM_AUTH);
        if (authToken != null) {
            TokenDTO tokenDTO = tokenCacheTool.getTokenCache(authToken);
            if (tokenDTO != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(tokenDTO.getUsername());
                userInfo.setUserId(tokenDTO.getUserId());
                userInfo.setRoleId(tokenDTO.getRoleId());
                userInfo.setTenantId(tokenDTO.getTenantId());
                result.put("userInfo", userInfo);
            }
        }

        return result;
    }

    /**
     * 清理用户所有平台Token
     *
     * @param username 用户名
     */
    public void cleanUserAllPlatformTokens(String username) {
        try {
            // 删除权限管理平台Token
            tokenCacheTool.deleteUserPlatformTokens(username, TokenCacheTool.PLATFORM_AUTH);

            // 删除接口管理平台Token
            tokenCacheTool.deleteUserPlatformTokens(username, TokenCacheTool.PLATFORM_API);

            // 删除用户所有Token（兼容旧版本）
            tokenCacheTool.deleteUserTokens(username);

            log.info("清理用户所有平台Token成功: {}", username);
        } catch (Exception e) {
            log.error("清理用户所有平台Token失败: {}", username, e);
        }
    }

    /**
     * 检查用户是否有权限访问目标平台
     *
     * @param username 用户名
     * @param targetPlatform 目标平台
     * @return 是否有权限
     */
    public boolean hasPlatformAccess(String username, String targetPlatform) {
        try {
            // 检查源平台（权限管理平台）Token是否有效
            String authToken = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, TokenCacheTool.PLATFORM_AUTH);
            if (authToken == null || !tokenCacheTool.hasAccessToken(authToken)) {
                return false;
            }

            // 如果是接口管理平台，需要特殊权限检查
            if (TokenCacheTool.PLATFORM_API.equals(targetPlatform)) {
                TokenDTO tokenDTO = tokenCacheTool.getTokenCache(authToken);
                if (tokenDTO != null) {
                    // 这里可以添加具体的权限检查逻辑
                    // 例如：检查用户角色、权限等
                    return true; // 简化处理，实际应该根据业务需求检查
                }
            }

            return true;
        } catch (Exception e) {
            log.error("检查平台访问权限失败: {}, {}", username, targetPlatform, e);
            return false;
        }
    }
}

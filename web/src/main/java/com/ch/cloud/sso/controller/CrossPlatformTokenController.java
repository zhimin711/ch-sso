package com.ch.cloud.sso.controller;

import com.ch.cloud.sso.biz.tools.TokenCacheTool;
import com.ch.cloud.sso.service.MultiPlatformTokenService;
import com.ch.e.PubError;
import com.ch.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 跨平台Token管理控制器
 * 处理用户权限管理平台和接口管理平台之间的Token同步
 *
 * @author zhimin
 * @since 2025/1/1
 */
@RestController
@RequestMapping("/api/cross-platform")
@Api(tags = "跨平台Token管理")
@Slf4j
public class CrossPlatformTokenController {

    @Autowired
    private MultiPlatformTokenService multiPlatformTokenService;

    @Autowired
    private TokenCacheTool tokenCacheTool;

    /**
     * 生成跨平台授权码
     * 用户权限管理平台调用，生成跳转到接口管理平台的授权码
     */
    @PostMapping("/auth-code/generate")
    @ApiOperation("生成跨平台授权码")
    public Result<String> generateAuthCode(
            @RequestParam String username,
            @RequestParam(defaultValue = "auth") String sourcePlatform,
            @RequestParam(defaultValue = "api") String targetPlatform) {
        try {
            // 检查用户是否有权限访问目标平台
            if (!multiPlatformTokenService.hasPlatformAccess(username, targetPlatform)) {
                return Result.error(PubError.NOT_AUTH,"用户没有权限访问目标平台");
            }

            String authCode = multiPlatformTokenService.generateCrossPlatformAuthCode(username, sourcePlatform, targetPlatform);
            if (authCode != null) {
                return Result.success(authCode);
            } else {
                return Result.error(PubError.INVALID,"生成授权码失败：源平台Token无效");
            }
        } catch (Exception e) {
            log.error("生成跨平台授权码失败: {}", username, e);
            return Result.error(PubError.ARGS,"生成授权码失败: " + e.getMessage());
        }
    }

    /**
     * 通过授权码获取Token
     * 接口管理平台调用，使用授权码获取访问Token
     */
    @PostMapping("/auth-code/exchange")
    @ApiOperation("通过授权码获取Token")
    public Result<Map<String, Object>> exchangeAuthCode(
            @RequestParam String authCode,
            @RequestParam(defaultValue = "api") String targetPlatform) {
        try {
            Map<String, Object> tokenInfo = multiPlatformTokenService.getTokenByAuthCode(authCode, targetPlatform);
            if (tokenInfo != null) {
                return Result.success(tokenInfo);
            } else {
                return Result.error(PubError.INVALID,"授权码无效或已过期");
            }
        } catch (Exception e) {
            log.error("通过授权码获取Token失败: {}", authCode, e);
            return Result.error(PubError.INVALID,"获取Token失败: " + e.getMessage());
        }
    }

    /**
     * 验证授权码
     */
    @GetMapping("/auth-code/validate")
    @ApiOperation("验证授权码")
    public Result<Map<String, Object>> validateAuthCode(@RequestParam String authCode) {
        try {
            Map<String, Object> authCodeInfo = tokenCacheTool.validateAuthCode(authCode);
            if (authCodeInfo != null) {
                return Result.success(authCodeInfo);
            } else {
                return Result.error(PubError.INVALID,"授权码无效或已过期");
            }
        } catch (Exception e) {
            log.error("验证授权码失败: {}", authCode, e);
            return Result.error(PubError.INVALID,"验证授权码失败: " + e.getMessage());
        }
    }

    /**
     * 刷新目标平台Token
     */
    @PostMapping("/token/refresh")
    @ApiOperation("刷新目标平台Token")
    public Result<Boolean> refreshTargetPlatformToken(
            @RequestParam String username,
            @RequestParam(defaultValue = "auth") String sourcePlatform,
            @RequestParam(defaultValue = "api") String targetPlatform) {
        try {
            boolean success = multiPlatformTokenService.refreshTargetPlatformToken(username, sourcePlatform, targetPlatform);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error(PubError.INVALID,"刷新Token失败：源平台Token无效");
            }
        } catch (Exception e) {
            log.error("刷新目标平台Token失败: {}", username, e);
            return Result.error(PubError.INVALID,"刷新Token失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户跨平台Token信息
     */
    @GetMapping("/user/info")
    @ApiOperation("获取用户跨平台Token信息")
    public Result<Map<String, Object>> getUserCrossPlatformInfo(@RequestParam String username) {
        try {
            Map<String, Object> userInfo = multiPlatformTokenService.getUserCrossPlatformInfo(username);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户跨平台信息失败: {}", username, e);
            return Result.error(PubError.INVALID,"获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 验证用户所有平台Token状态
     */
    @GetMapping("/user/validate")
    @ApiOperation("验证用户所有平台Token状态")
    public Result<Map<String, Boolean>> validateUserAllPlatformTokens(@RequestParam String username) {
        try {
            Map<String, Boolean> platformStatus = multiPlatformTokenService.validateUserAllPlatformTokens(username);
            return Result.success(platformStatus);
        } catch (Exception e) {
            log.error("验证用户平台Token状态失败: {}", username, e);
            return Result.error(PubError.INVALID,"验证Token状态失败: " + e.getMessage());
        }
    }

    /**
     * 清理用户所有平台Token
     */
    @DeleteMapping("/user/clean")
    @ApiOperation("清理用户所有平台Token")
    public Result<Void> cleanUserAllPlatformTokens(@RequestParam String username) {
        try {
            multiPlatformTokenService.cleanUserAllPlatformTokens(username);
            return Result.success();
        } catch (Exception e) {
            log.error("清理用户所有平台Token失败: {}", username, e);
            return Result.error(PubError.INVALID,"清理Token失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户平台访问权限
     */
    @GetMapping("/user/access")
    @ApiOperation("检查用户平台访问权限")
    public Result<Boolean> checkPlatformAccess(
            @RequestParam String username,
            @RequestParam String targetPlatform) {
        try {
            boolean hasAccess = multiPlatformTokenService.hasPlatformAccess(username, targetPlatform);
            return Result.success(hasAccess);
        } catch (Exception e) {
            log.error("检查用户平台访问权限失败: {}, {}", username, targetPlatform, e);
            return Result.error(PubError.INVALID,"检查访问权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定平台Token
     */
    @GetMapping("/user/platform-token")
    @ApiOperation("获取用户指定平台Token")
    public Result<String> getUserPlatformToken(
            @RequestParam String username,
            @RequestParam String platform) {
        try {
            String token = tokenCacheTool.getAccessTokenByUsernameAndPlatform(username, platform);
            if (token != null) {
                return Result.success(token);
            } else {
                return Result.error(PubError.INVALID,"用户在该平台没有有效的Token");
            }
        } catch (Exception e) {
            log.error("获取用户平台Token失败: {}, {}", username, platform, e);
            return Result.error(PubError.INVALID,"获取Token失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户所有平台Token
     */
    @GetMapping("/user/all-platform-tokens")
    @ApiOperation("获取用户所有平台Token")
    public Result<Map<String, String>> getUserAllPlatformTokens(@RequestParam String username) {
        try {
            Map<String, String> platformTokens = tokenCacheTool.getUserAllPlatformTokens(username);
            return Result.success(platformTokens);
        } catch (Exception e) {
            log.error("获取用户所有平台Token失败: {}", username, e);
            return Result.error(PubError.INVALID,"获取Token失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户指定平台Token
     */
    @DeleteMapping("/user/platform-token")
    @ApiOperation("删除用户指定平台Token")
    public Result<Void> deleteUserPlatformToken(
            @RequestParam String username,
            @RequestParam String platform) {
        try {
            tokenCacheTool.deleteUserPlatformTokens(username, platform);
            return Result.success();
        } catch (Exception e) {
            log.error("删除用户平台Token失败: {}, {}", username, platform, e);
            return Result.error(PubError.INVALID,"删除Token失败: " + e.getMessage());
        }
    }
} 
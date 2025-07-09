package com.ch.cloud.sso.controller;

import com.ch.cloud.sso.biz.manager.TokenManager;
import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.biz.tools.TokenCacheTool;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.e.PubError;
import com.ch.result.Result;
import io.swagger.v3.oas.annotations.Operation; // 修改: 替换 ApiOperation 为 Operation
import io.swagger.v3.oas.annotations.tags.Tag; // 修改: 替换 Api 为 Tag
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Token管理控制器
 * 展示如何使用TokenManager和TokenRedisUtil
 *
 * @author zhimin
 * @since 2025/1/1
 */
@RestController
@RequestMapping("/api/token")
@Tag(name = "Token管理", description = "Token相关接口") // 修改: 替换 Api 为 Tag，并添加 description
@Slf4j
public class TokenController {

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private TokenCacheTool tokenCacheTool;

    /**
     * 生成Token
     */
    @PostMapping("/generate")
    @Operation(summary = "生成Token", description = "生成访问令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<TokenVo> generateToken(@RequestBody UserInfo userInfo,
                                        @RequestParam String secret) {
        try {
            TokenVo tokenVo = tokenManager.generateToken(userInfo, secret);
            return Result.success(tokenVo);
        } catch (Exception e) {
            log.error("生成Token失败", e);
            return Result.error(PubError.INVALID,"生成Token失败: " + e.getMessage());
        }
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "验证访问令牌的有效性") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<Boolean> validateToken(@RequestParam String token) {
        try {
            boolean isValid = tokenManager.validateToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return Result.error(PubError.INVALID,"验证Token失败: " + e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "刷新访问令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<TokenVo> refreshToken(@RequestParam String refreshToken) {
        try {
            TokenVo tokenVo = tokenManager.refreshToken(refreshToken);
            if (tokenVo != null) {
                return Result.success(tokenVo);
            } else {
                return Result.error(PubError.INVALID,"刷新Token失败：Token无效或已过期");
            }
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return Result.error(PubError.INVALID,"刷新Token失败: " + e.getMessage());
        }
    }

    /**
     * 续期刷新Token
     */
    @PostMapping("/renew")
    @Operation(summary = "续期刷新Token", description = "续期刷新访问令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<Boolean> renewRefreshToken(@RequestParam String refreshToken) {
        try {
            boolean success = tokenManager.renewToken(refreshToken);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error(PubError.INVALID,"续期失败：Token无效或不存在");
            }
        } catch (Exception e) {
            log.error("续期刷新Token失败", e);
            return Result.error(PubError.INVALID,"续期刷新Token失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "通过Token获取用户信息") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<UserInfo> getUserInfo(@RequestParam String token) {
        try {
            UserInfo userInfo = tokenManager.getUserInfo(token);
            if (userInfo != null) {
                return Result.success(userInfo);
            } else {
                return Result.error(PubError.INVALID,"获取用户信息失败：Token无效或已过期");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(PubError.INVALID,"获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除Token
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除Token", description = "删除指定的访问令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<Void> deleteToken(@RequestParam String token) {
        try {
            tokenManager.deleteToken(token);
            return Result.success();
        } catch (Exception e) {
            log.error("删除Token失败", e);
            return Result.error(PubError.INVALID,"删除Token失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户的所有Token
     */
    @DeleteMapping("/delete-user-tokens")
    @Operation(summary = "删除用户的所有Token", description = "删除指定用户的全部访问令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<Void> deleteUserTokens(@RequestParam String username) {
        try {
            tokenManager.deleteUserTokens(username);
            return Result.success();
        } catch (Exception e) {
            log.error("删除用户Token失败", e);
            return Result.error(PubError.INVALID,"删除用户Token失败: " + e.getMessage());
        }
    }

    /**
     * 获取Token过期时间
     */
    @GetMapping("/expire-time")
    @Operation(summary = "获取Token过期时间", description = "获取访问令牌的剩余有效时间") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<Long> getTokenExpireTime(@RequestParam String token) {
        try {
            long expireTime = tokenCacheTool.getAccessTokenExpireTime(token);
            return Result.success(expireTime);
        } catch (Exception e) {
            log.error("获取Token过期时间失败", e);
            return Result.error(PubError.INVALID,"获取Token过期时间失败: " + e.getMessage());
        }
    }

    /**
     * 获取刷新Token过期时间
     */
    @GetMapping("/refresh-expire-time")
    @Operation(summary = "获取刷新Token过期时间", description = "获取刷新令牌的剩余有效时间") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<Long> getRefreshTokenExpireTime(@RequestParam String refreshToken) {
        try {
            long expireTime = tokenCacheTool.getRefreshTokenExpireTime(refreshToken);
            return Result.success(expireTime);
        } catch (Exception e) {
            log.error("获取刷新Token过期时间失败", e);
            return Result.error(PubError.INVALID,"获取刷新Token过期时间失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户名获取访问Token
     */
    @GetMapping("/access-token")
    @Operation(summary = "根据用户名获取访问Token", description = "通过用户名获取用户的访问令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<String> getAccessTokenByUsername(@RequestParam String username) {
        try {
            String token = tokenCacheTool.getAccessTokenByUsername(username);
            if (token != null) {
                return Result.success(token);
            } else {
                return Result.error(PubError.INVALID,"用户没有有效的访问Token");
            }
        } catch (Exception e) {
            log.error("获取访问Token失败", e);
            return Result.error(PubError.INVALID,"获取访问Token失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户名获取刷新Token
     */
    @GetMapping("/refresh-token")
    @Operation(summary = "根据用户名获取刷新Token", description = "通过用户名获取用户的刷新令牌") // 修改: 替换 ApiOperation 为 Operation，并添加 summary 和 description
    public Result<String> getRefreshTokenByUsername(@RequestParam String username) {
        try {
            String refreshToken = tokenCacheTool.getRefreshTokenByUsername(username);
            if (refreshToken != null) {
                return Result.success(refreshToken);
            } else {
                return Result.error(PubError.INVALID,"用户没有有效的刷新Token");
            }
        } catch (Exception e) {
            log.error("获取刷新Token失败", e);
            return Result.error(PubError.INVALID,"获取刷新Token失败: " + e.getMessage());
        }
    }
}
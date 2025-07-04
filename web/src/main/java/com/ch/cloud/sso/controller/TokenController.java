package com.ch.cloud.sso.controller;

import com.ch.cloud.sso.biz.manager.TokenManager;
import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.biz.tools.TokenCacheTool;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.e.PubError;
import com.ch.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "Token管理")
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
    @ApiOperation("生成Token")
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
    @ApiOperation("验证Token")
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
    @ApiOperation("刷新Token")
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
    @ApiOperation("续期刷新Token")
    public Result<Boolean> renewRefreshToken(@RequestParam String refreshToken) {
        try {
            boolean success = tokenManager.renewRefreshToken(refreshToken);
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
    @ApiOperation("获取用户信息")
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
    @ApiOperation("删除Token")
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
    @ApiOperation("删除用户的所有Token")
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
    @ApiOperation("获取Token过期时间")
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
    @ApiOperation("获取刷新Token过期时间")
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
    @ApiOperation("根据用户名获取访问Token")
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
    @ApiOperation("根据用户名获取刷新Token")
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
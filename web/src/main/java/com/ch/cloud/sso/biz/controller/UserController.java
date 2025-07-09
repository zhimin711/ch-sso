package com.ch.cloud.sso.biz.controller;

import com.ch.Constants;
import com.ch.cloud.sso.biz.manager.TokenManager;
import com.ch.cloud.sso.biz.manager.UserManager;
import com.ch.cloud.sso.biz.mq.GatewayNotifySender;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.biz.pojo.UserPermissionVo;
import com.ch.cloud.sso.biz.pojo.UserVo;
import com.ch.cloud.sso.biz.service.IUserService;
import com.ch.cloud.sso.biz.tools.TokenTool;
import com.ch.pojo.KeyValue;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * desc: 用户信息控制器
 *
 * @author zhimin
 * @since 2018/12/21 10:40 PM
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户信息", description = "用户信息相关接口")
@Slf4j
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    IUserService userService;

    @Autowired
    private GatewayNotifySender gatewayNotifySender;

    @Autowired
    private TokenTool tokenTool;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private UserManager userManager;

    /**
     * 资源服务器提供的受保护接口
     *
     * @param principal 授权信息
     * @return 授权信息
     */
    @GetMapping("/")
    public Principal user(Principal principal) {
        logger.debug("授权用户信息: {}", principal);
        return principal;
    }

    @Operation(summary = "获取用户授权", description = "用户授权信息")
    @GetMapping("/info")
    public Result<UserVo> info() {
        return ResultUtils.wrapFail(() -> userService.findUserInfo(ContextUtil.getUsername()));
    }

    @Operation(summary = "访问令牌获取用户授权", description = "访问令牌获取,返回用户授权信息")
    @PostMapping("/permissions")
    public Result<UserPermissionVo> permissions(@RequestHeader(Constants.X_TOKEN) String token,
                                                @RequestBody UserInfo user) {
        return ResultUtils.wrapFail(() -> {
            user.setUsername(ContextUtil.getUsername());
            // 判断用户角色是否切换角色，如果切换了角色则通知网关清除对应的用户角色缓存
            boolean refresh = tokenTool.refreshUserRole(ContextUtil.getUsername(), user.getRoleId());
            if (refresh) {
                try {
                    // 通知网关清除对应的用户角色缓存
                    gatewayNotifySender.cleanNotify(new KeyValue("users", token));
                } catch (Exception e) {
                    log.error("cleanNotify", e);
                }
            }
            return userService.findPermission(user);
        });
    }


    @Operation(summary = "获取授权码", description = "获取授权码")
    @GetMapping(value = "/auth-code")
    public Result<String> authCode(@RequestHeader(Constants.X_TOKEN) String token,
                                   @RequestParam String url) {
        return ResultUtils.wrapFail(() -> {
            tokenManager.validateToken(token);
            return userManager.generateAuthCode(ContextUtil.getUsername(), url);
        });
    }

}

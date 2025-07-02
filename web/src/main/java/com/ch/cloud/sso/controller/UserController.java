package com.ch.cloud.sso.controller;

import com.ch.Constants;
import com.ch.cloud.sso.mq.GatewayNotifySender;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.pojo.UserPermissionVo;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.TokenTool;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.pojo.KeyValue;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * desc: 用户信息控制器
 *
 * @author zhimin
 * @since 2018/12/21 10:40 PM
 */
@RestController
@RequestMapping("/user")
@Api("用户信息")
@Slf4j
public class UserController {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    IUserService userService;
    
    @Autowired
    private GatewayNotifySender gatewayNotifySender;
    
    @Autowired
    private TokenTool tokenTool;
    
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
    
    /**
     * 登录用户信息
     *
     * @return 用户信息
     */
    @ApiOperation(value = "获取用户授权", notes = "用户授权信息")
    @GetMapping("/info")
    public Result<UserVo> info() {
        return ResultUtils.wrapFail(() -> userService.findUserInfo(ContextUtil.getUsername()));
    }
    
    /**
     * 获取用户的角色菜单与权限
     *
     * @return 角色菜单与权限
     */
    @ApiOperation(value = "访问令牌获取用户授权", notes = "访问令牌获取,返回用户授权信息")
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
    
}

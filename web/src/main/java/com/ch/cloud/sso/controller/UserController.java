package com.ch.cloud.sso.controller;

import com.ch.Constants;
import com.ch.cloud.sso.mq.GatewayNotifySender;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.pojo.UserPermissionVo;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.TokenTool;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.pojo.KeyValue;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
     * 用户授权信息
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    @ApiOperation(value = "访问令牌获取用户授权", notes = "访问令牌获取,返回用户授权信息")
    @GetMapping("/info")
    public Result<UserVo> info(@RequestHeader(Constants.X_TOKEN) String token) {
        return ResultUtils.wrapFail(() -> {
            String username = userService.validate(token);
            AssertUtils.isEmpty(username, PubError.INVALID, "访问令牌已失效!");
            return userService.findUserInfo(username);
        });
    }
    
    /**
     * 获取用户菜单与权限
     *
     * @return 用户菜单与权限
     */
    @ApiOperation(value = "访问令牌获取用户授权", notes = "访问令牌获取,返回用户授权信息")
    @PostMapping("/permissions")
    public Result<UserPermissionVo> permissions(@RequestHeader(Constants.X_TOKEN) String token,
            @RequestBody UserInfo user) {
        return ResultUtils.wrapFail(() -> {
            String username = userService.validate(token);
            if (CommonUtils.isEmpty(username)) {
                ExceptionUtils._throw(PubError.INVALID, "访问令牌已失效!");
            }
            user.setUsername(username);
//            UserVo userVo = userService.findUserInfo(username);
//            user.setUserId(userVo.getUserId());
            boolean refresh = tokenTool.refreshUserRole(username, user.getRoleId());
            if (refresh) {
                try {
                    gatewayNotifySender.cleanNotify(new KeyValue("users", token));
                } catch (Exception e) {
                    log.error("cleanNotify", e);
                }
            }
            return userService.findPermission(user);
        });
    }
    
}

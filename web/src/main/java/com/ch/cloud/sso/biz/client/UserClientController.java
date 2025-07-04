package com.ch.cloud.sso.biz.client;

import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.biz.pojo.UserVo;
import com.ch.cloud.sso.biz.service.IUserService;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.BeanUtilsV2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * desc: 用户信息控制器
 *
 * @author zhimin
 * @since 2018/12/21 10:40 PM
 */
@RestController
@RequestMapping("/fc/user")
@Api("用户信息")
@Slf4j
public class UserClientController implements SsoUserClient {
    
    @Autowired
    IUserService userService;
    
    /**
     * 用户授权信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @GetMapping("/info")
    public Result<UserInfo> info(@RequestParam("username") String username) {
        return ResultUtils.wrapFail(() -> {
            UserVo user = userService.findUserInfo(username);
            
            return BeanUtilsV2.clone(user, UserInfo.class);
        });
    }
    
    
}

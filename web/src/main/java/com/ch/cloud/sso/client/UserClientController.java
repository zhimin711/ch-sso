package com.ch.cloud.sso.client;

import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
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
     * @param token 访问令牌
     * @return 用户信息
     */
    @ApiOperation(value = "访问令牌获取用户授权", notes = "访问令牌获取,返回用户授权信息")
    @GetMapping("/info")
    public Result<UserInfo> info(@RequestParam("token") String token) {
        return ResultUtils.wrapFail(() -> {
            String username = userService.validate(token);
            AssertUtils.isEmpty(username, PubError.INVALID, "访问令牌已失效!");
            UserVo user = userService.findUserInfo(username);
            
            return BeanUtilsV2.clone(user, UserInfo.class);
        });
    }
    
    
}

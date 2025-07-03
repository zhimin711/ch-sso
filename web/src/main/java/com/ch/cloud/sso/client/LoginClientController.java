package com.ch.cloud.sso.client;

import com.ch.Constants;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.TokenTool;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * desc:登录与授权服务
 *
 * @author zhimin
 * @since 2018/12/21 10:40 PM
 */
@RestController
@Slf4j
@Api("用户登录")
@RequestMapping("/fc/login/token")
public class LoginClientController implements SsoLoginClient {
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private TokenTool tokenTool;
    
    @GetMapping(value = "validate")
    public Result<String> validate(@RequestHeader(Constants.X_TOKEN) String token) {
        return Result.success(userService.validate(token));
    }
    
    /**
     * 网关获取Token包含详细信息
     *
     * @param token 令牌
     * @return user info
     */
    @GetMapping(value = "info")
    public Result<UserInfo> info(@RequestHeader(Constants.X_TOKEN) String token) {
        return ResultUtils.wrapFail(() -> {
            String username = userService.validate(token);
            Assert.notEmpty(username, PubError.INVALID, "访问令牌已",token);
            return userService.extractToken(token);
        });
    }
    
    /**
     * 刷新 token 缓存时间（续期）
     * @param token 令牌
     * @return 操作结果
     */
    @GetMapping("renew")
    @Override
    public Result<Boolean> renew(@RequestHeader(Constants.X_TOKEN) String token) {
        return ResultUtils.wrap(() -> tokenTool.renew(token));
    }
    
}

package com.ch.cloud.sso.biz.client;

import com.ch.Constants;
import com.ch.cloud.sso.biz.manager.TokenManager;
import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.biz.service.IUserService;
import com.ch.cloud.sso.client.SsoLoginClient;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag; // 修改: 替换为 Swagger 3.0 的 Tag 注解
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
@Tag(name = "用户登录", description = "用户登录相关接口") // 修改: 替换 Api 为 Tag
@RequestMapping("/fc/login/token")
public class LoginClientController implements SsoLoginClient {

    @Autowired
    private IUserService userService;

    @Autowired
    private TokenManager tokenManager;

    @GetMapping(value = "validate")
    @Override
    public Result<Boolean> validate(@RequestHeader(Constants.X_TOKEN) String token) {
        return Result.success(tokenManager.validateToken(token));
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

            boolean isValid = tokenManager.validateToken(token);
//            String username = userService.validate(token);
            Assert.isTrue(isValid, PubError.INVALID, "访问令牌已",token);
            return tokenManager.getUserInfo(token);
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
        return ResultUtils.wrap(() -> tokenManager.renewToken(token));
    }

    @GetMapping("refresh")
    @Override
    public Result<String> refresh(@RequestHeader(Constants.X_TOKEN) String token,
                                   @RequestHeader(Constants.X_REFRESH_TOKEN) String refreshToken) {
        return ResultUtils.wrap(() -> {
            TokenVo tokenVo = tokenManager.refreshToken(refreshToken);
            return tokenVo.getToken();
        });
    }

}

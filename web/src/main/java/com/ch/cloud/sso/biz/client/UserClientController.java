package com.ch.cloud.sso.biz.client;

import com.ch.cloud.sso.client.SsoUserClient;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.biz.pojo.UserVo;
import com.ch.cloud.sso.biz.service.IUserService;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.BeanUtilsV2;
import io.swagger.v3.oas.annotations.Operation; // 修改: 替换为 Swagger 3.0 的 Operation 注解
import io.swagger.v3.oas.annotations.tags.Tag; // 修改: 替换为 Swagger 3.0 的 Tag 注解
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
@Tag(name = "用户信息", description = "用户信息相关接口") // 修改: 替换 Api 为 Tag
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
    @Operation(summary = "获取用户信息", description = "获取用户信息") // 修改: 替换 ApiOperation 为 Operation
    @GetMapping("/info")
    public Result<UserInfo> info(@RequestParam("username") String username) {
        return ResultUtils.wrapFail(() -> {
            UserVo user = userService.findUserInfo(username);

            return BeanUtilsV2.clone(user, UserInfo.class);
        });
    }

}
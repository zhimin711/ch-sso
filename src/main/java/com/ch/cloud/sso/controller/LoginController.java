package com.ch.cloud.sso.controller;

import com.ch.Constants;
import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.ExceptionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

/**
 * desc:
 *
 * @author zhimin
 * @date 2018/12/21 10:40 PM
 */
@RestController
@Slf4j
@Api("用户登录")
public class LoginController {


    @Autowired
    private JwtTokenTool jwtTokenTool;

    @Autowired
    IUserService userService;


    //
    @GetMapping("login")
    @ApiIgnore
    public ModelAndView index() {
        return new ModelAndView("login");
    }

    /**
     * 获取用户访问令牌
     * 密码模式登录
     * <p>
     * //     * @param username
     * //     * @param password
     *
     * @return access_token
     */
    @ApiOperation(value = "获取用户访问令牌", notes = "基于密码模式登录,无需签名,返回access_token")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "username", required = true, value = "登录名", paramType = "form"),
//            @ApiImplicitParam(name = "password", required = true, value = "登录密码", paramType = "form")
//    })
    @PostMapping(value = "login/token/access", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<TokenVo> getLoginToken(@RequestBody UserDto user) {

        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            return Result.error(PubError.USERNAME_OR_PASSWORD, "用户或者密码不能为空！");
        }
        return ResultUtils.wrap(() -> userService.login(user.getUsername(), user.getPassword()));
    }


    @ApiOperation(value = "刷新访问令牌", notes = "刷新访问令牌")
    @GetMapping(value = "login/token/refresh")
    public Result<TokenVo> refresh(@RequestParam String token, @RequestParam String refreshToken) {
        return ResultUtils.wrapFail(() -> {
            TokenVo tokenVo = new TokenVo();
            tokenVo.setToken(token);
            tokenVo.setRefreshToken(refreshToken);
            if(jwtTokenTool.isTokenExpired(tokenVo.getRefreshToken())){
                ExceptionUtils._throw(PubError.EXPIRED, "刷新令牌已失效!");
            }
            userService.refreshToken(tokenVo);
            return tokenVo;
        });
    }

    @GetMapping(value = "login/token/validate")
    public Result<String> validate(@RequestParam String token) {
        return Result.success(userService.validate(token));
    }

    @ApiOperation(value = "访问令牌获取用户授权", notes = "访问令牌获取,返回用户授权信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", required = true, value = "访问令牌", paramType = "query"),
            @ApiImplicitParam(name = "role", required = true, value = "访问角色", paramType = "query")
    })
    @GetMapping("login/token/user")
    public Result<UserVo> login(@RequestHeader(Constants.TOKEN_HEADER2) String token, @RequestParam Long role) {
        String username = userService.validate(token);
        if (CommonUtils.isNotEmpty(username)) {
            return Result.success(userService.findUserInfo(username, role));
        }
        return Result.error(PubError.INVALID, "访问令牌已失效!");
    }

    @GetMapping(value = "login/token/info")
    public Result<UserInfo> info(@RequestHeader(Constants.TOKEN_HEADER2) String token) {
        return ResultUtils.wrapFail(() -> userService.extractToken(token));
    }

}

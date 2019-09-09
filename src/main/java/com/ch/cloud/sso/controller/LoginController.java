package com.ch.cloud.sso.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.cli.UpmsClientService;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.utils.JwtUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.ExceptionUtils;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @NacosValue(value = "${security.oauth2.client.client-id}", autoRefreshed = true)
    private String clientId;
    @NacosValue(value = "${security.oauth2.client.client-id}", autoRefreshed = true)
    private String clientSecret;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UpmsClientService upmsClientService;

    //
    @GetMapping("login")
    @ApiIgnore
    public ModelAndView index() {
        return new ModelAndView("login");
    }

    /**
     * 获取用户访问令牌
     * 基于oauth2密码模式登录
     *
     * @param username
     * @param password
     * @return access_token
     */
    @ApiOperation(value = "获取用户访问令牌", notes = "基于oauth2密码模式登录,无需签名,返回access_token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", required = true, value = "登录名", paramType = "form"),
            @ApiImplicitParam(name = "password", required = true, value = "登录密码", paramType = "form")
    })
    @PostMapping(value = "login/token/access", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Long> getLoginToken(@RequestParam String username, @RequestParam String password, @RequestHeader HttpHeaders headers) {
        // 使用oauth2密码模式登录.
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("username", username);
        postParameters.add("password", password);
        postParameters.add("client_id", clientId);
        postParameters.add("client_secret", clientSecret);
        postParameters.add("grant_type", "password");
        // 使用客户端的请求头,发起请求
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 强制移除 原来的请求头,防止token失效
        headers.remove(HttpHeaders.AUTHORIZATION);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        return ResultUtils.wrap(() -> 1L);
    }


    @ApiOperation(value = "获取用户访问Jwt令牌", notes = "基于Jwt登录,无需签名,返回token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", required = true, value = "登录名", paramType = "form"),
            @ApiImplicitParam(name = "password", required = true, value = "登录密码", paramType = "form")
    })
    @PostMapping("login")
    public Result<String> login(@RequestParam String username,
                                @RequestParam String password) {
        return ResultUtils.wrapList(() -> {

            Result<UserDto> res = upmsClientService.findUserByUsername(username);
            if (res.isEmpty()) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            List<String> tokens = Lists.newArrayList();
            //生成JWT
            String token = JwtUtils.createJWT(600000, res.get());
            //生成refreshToken
            String refreshToken = UUID.randomUUID().toString().replaceAll("-", "");
            //保存refreshToken至redis，使用hash结构保存使用中的token以及用户标识
//            String refreshTokenKey = String.format(jwtRefreshTokenKeyFormat, refreshToken);
//            stringRedisTemplate.opsForHash().put(refreshTokenKey, "token", token);
//            stringRedisTemplate.opsForHash().put(refreshTokenKey, "username", username);
            //refreshToken设置过期时间
//            stringRedisTemplate.expire(refreshTokenKey, refreshTokenExpireTime, TimeUnit.MILLISECONDS);

            tokens.add(token);
            tokens.add(refreshToken);
            return tokens;
        });
        //账号密码校验
    }

    @Autowired
    IUserService userService;


    @PostMapping(value = "${jwt.route.login}")
    public Result<String> login(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return Result.error(PubError.USERNAME_OR_PASSWORD, "用户或者密码不能为空！");
        }
        return Result.success(userService.login(username, password));
    }

    @PostMapping(value = "${jwt.route.refresh}")
    public Result<String> refresh(@RequestHeader("${jwt.header}") String token) {
        return Result.success(userService.refreshToken(token));
    }

}

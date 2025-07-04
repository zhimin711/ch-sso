package com.ch.cloud.sso.biz.controller;

import com.ch.Constants;
import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.biz.tools.TokenTool;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * desc:
 *
 * @author zhimin
 * @since 2018/12/21 10:40 PM
 */
@RestController
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TokenTool tokenTool;


    @GetMapping("oauth/logout")
    public Result<String> revokeToken(HttpServletRequest request,
                                      @RequestHeader(Constants.X_TOKEN) String token) {
        if (CommonUtils.isEmpty(token) || !token.startsWith("Bearer ")) {
            logger.error("error token: {}", token);
            return Result.error(PubError.INVALID, "token invalid!");
        }
        String tokenId = token.substring("Bearer".length() + 1);
        /*if (consumerTokenServices.revokeToken(tokenId)) {
            new SecurityContextLogoutHandler().logout(request, null, null);
            return Result.success("注销成功");
        } else */
        {
            return Result.error(PubError.UNKNOWN);
        }
    }


    @PostMapping("logout/token")
    public Result<String> revokeToken2(
            @RequestHeader(Constants.X_TOKEN) String token,
            @RequestBody TokenVo tokenVo) {
        //todo revoke token and refresh token
        /*if (CommonUtils.isEmpty(token) || !token.startsWith("Bearer ")) {
            logger.error("error token: {}", token);
            return Result.error(PubError.INVALID, "token invalid!");
        }*/
        tokenVo.setToken(token);
        tokenTool.invalid(tokenVo);
        return Result.success();
    }

}

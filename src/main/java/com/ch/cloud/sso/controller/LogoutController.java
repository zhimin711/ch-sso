package com.ch.cloud.sso.controller;

import com.ch.Constants;
import com.ch.Status;
import com.ch.e.CoreError;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * desc:
 *
 * @author zhimin
 * @date 2018/12/21 10:40 PM
 */
@RestController
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

//    @Autowired
//    IUserService userService;
    //    @Autowired
//    PasswordService passwordService;

    @Autowired
    @Qualifier("consumerTokenServices")
    ConsumerTokenServices consumerTokenServices;

    @GetMapping("oauth/logout")
    public Result<String> revokeToken(HttpServletRequest request,
                                      @RequestHeader(Constants.TOKEN_HEADER) String token) {
        if (CommonUtils.isEmpty(token) || !token.startsWith("Bearer ")) {
            logger.error("error token: {}", token);
            return new Result<>(CoreError.INVALID, "token invalid!");
        }
        String tokenId = token.substring("Bearer".length() + 1);
        if (consumerTokenServices.revokeToken(tokenId)) {
            new SecurityContextLogoutHandler().logout(request, null, null);
            return new Result<>("注销成功");
        } else {
            return new Result<>(Status.ERROR);
        }
    }

}

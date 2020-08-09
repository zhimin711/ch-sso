package com.ch.cloud.sso.controller;

import com.ch.Constants;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("oauth/logout")
    public Result<String> revokeToken(HttpServletRequest request,
                                      @RequestHeader(Constants.TOKEN_HEADER) String token) {
        if (CommonUtils.isEmpty(token) || !token.startsWith("Bearer ")) {
            logger.error("error token: {}", token);
            return  Result.error(PubError.INVALID, "token invalid!");
        }
        String tokenId = token.substring("Bearer".length() + 1);
        /*if (consumerTokenServices.revokeToken(tokenId)) {
            new SecurityContextLogoutHandler().logout(request, null, null);
            return Result.success("注销成功");
        } else */{
            return Result.error(PubError.UNKNOWN);
        }
    }


    @PostMapping("logout/token")
    public Result<String> revokeToken2(@RequestHeader(Constants.TOKEN_HEADER2) String token,
                                       @RequestParam("refreshToken") String refreshToken) {
        //todo revoke token and refresh token
        /*if (CommonUtils.isEmpty(token) || !token.startsWith("Bearer ")) {
            logger.error("error token: {}", token);
            return  Result.error(PubError.INVALID, "token invalid!");
        }*/
        return Result.success();
    }

}

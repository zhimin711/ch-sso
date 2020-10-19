package com.ch.cloud.sso.controller;

import com.ch.Constants;
import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.pojo.LoginDto;
import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.cloud.sso.utils.CaptchaUtils;
import com.ch.cloud.sso.utils.SlideCaptchaUtil;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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
    @PostMapping(value = "login/token/access", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<TokenVo> getLoginToken(@RequestBody LoginDto user, HttpServletRequest request) {

        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            return Result.error(PubError.USERNAME_OR_PASSWORD, "用户或者密码不能为空！");
        }
        String verifyCode = (String) request.getSession().getAttribute("verifyCode");
        if (StringUtils.isEmpty(user.getCaptchaCode()) || StringUtils.isEmpty(verifyCode) || !verifyCode.equalsIgnoreCase(user.getCaptchaCode())) {
            return Result.error(PubError.NOT_, "验证码错误或已过期！");
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
            if (jwtTokenTool.isTokenExpired(tokenVo.getRefreshToken())) {
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
        return ResultUtils.wrapFail(() -> {
            Long r = role;
            String username = userService.validate(token);
            if (CommonUtils.isEmpty(username)) {
                ExceptionUtils._throw(PubError.INVALID, "访问令牌已失效!");
            }
            UserInfo user = userService.extractToken(token);
            if (r == 0 && user.getRoleId() > 0) {
                r = user.getRoleId();
            }
            UserVo userVo = userService.findUserInfo(username, r);
            userVo.setPassword(null);
            if (!CommonUtils.isEquals(r, user.getRoleId())) {
                user.setRoleId(r);
                String newToken = jwtTokenTool.generateToken(user);
                userVo.setToken(newToken);
            }
            return userVo;
        });
    }

    @GetMapping(value = "login/token/info")
    public Result<UserInfo> info(@RequestHeader(Constants.TOKEN_HEADER2) String token) {
        return ResultUtils.wrapFail(() -> userService.extractToken(token));
    }


    @GetMapping("login/captcha")
    public List<String> getCaptcha(HttpServletRequest request, HttpServletResponse response, @RequestParam String captchaKey) {
        try {
            int width = 200;
            int height = 69;

            BufferedImage verifyImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            //生成对应宽高的初始图片
            String randomText = CaptchaUtils.drawRandomText(width, height, verifyImg);

            //单独的一个类方法，出于代码复用考虑，进行了封装。
            //功能是生成验证码字符并加上噪点，干扰线，返回值为验证码字符
            request.getSession().setAttribute("verifyCode", randomText);
            response.setContentType("image/png");//必须设置响应内容类型为图片，否则前台不识别

            OutputStream os = response.getOutputStream(); //获取文件输出流
            ImageIO.write(verifyImg, "png", os);//输出图片流

            os.flush();
            os.close();//关闭流
        } catch (IOException e) {
            log.error("generate captcha error!", e);
        }
        return null;
    }

    @GetMapping("login/slideCaptcha")
    public Result<?> getSlideCaptcha(HttpServletRequest request, HttpServletResponse response) {

//        return ResultUtils.wrapFail(SlideCaptchaUtil::build);
        return ResultUtils.wrapFail(CaptchaUtils::selectSlideVerificationCode);
    }
}

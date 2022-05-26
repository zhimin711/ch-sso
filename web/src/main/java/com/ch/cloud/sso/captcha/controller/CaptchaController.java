package com.ch.cloud.sso.captcha.controller;

import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.ch.cloud.sso.captcha.service.CaptchaService;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * decs:验证码控制层.
 *
 * @author zhimin.ma
 * @since 2021/1/1
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    @PostMapping("/get")
    public Result<?> get(@RequestBody CaptchaVO captchaVO) {
        return ResultUtils.wrapFail(() -> captchaService.get(captchaVO));
    }

    @PostMapping("/check")
    public Result<?> check(@RequestBody CaptchaVO captchaVO) {
        return ResultUtils.wrapFail(() -> captchaService.check(captchaVO));
    }

    @PostMapping("/verify")
    public Result<?> verify(@RequestBody CaptchaVO captchaVO) {
        return ResultUtils.wrapFail(() -> captchaService.verification(captchaVO.getCaptchaVerification()));
    }

}

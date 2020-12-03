package com.ch.cloud.sso.captcha.controller;

import com.ch.cloud.sso.captcha.model.common.ResponseModel;
import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.ch.cloud.sso.captcha.service.CaptchaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * decs:验证码控制层.
 *
 * @author zhimin.ma
 * @date 2021/1/1
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO captchaVO) {
        return captchaService.get(captchaVO);
    }

    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO captchaVO) {
        return captchaService.check(captchaVO);
    }

    @PostMapping("/verify")
    public ResponseModel verify(@RequestBody CaptchaVO captchaVO) {
        return captchaService.verification(captchaVO);
    }

}

package com.ch.cloud.sso.captcha.service;

import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;

import java.util.Properties;

/**
 * decs:验证码服务接口
 *
 * @author zhimin.ma
 * @since 2021/1/1
 */
public interface CaptchaService {
    /**
     * 配置初始化
     */
    void init(Properties config);

    /**
     * 获取验证码
     *
     * @param captchaVO 验证码
     * @return 验证码
     */
    CaptchaVO get(CaptchaVO captchaVO);

    /**
     * 核对验证码(前端)
     *
     * @param captchaVO 验证码
     * @return 验证码
     */
    CaptchaVO check(CaptchaVO captchaVO);

    /**
     * 二次校验验证码(后端)
     *
     * @param captchaVerification 二次验证码
     * @return 验证码
     */
    boolean verification(String captchaVerification);

    /***
     * 验证码类型
     * 通过java SPI机制，接入方可自定义实现类，实现新的验证类型
     * @return 验证码类型
     */
    String captchaType();

}

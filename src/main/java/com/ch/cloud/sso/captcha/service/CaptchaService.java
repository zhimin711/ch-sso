package com.ch.cloud.sso.captcha.service;

import com.ch.cloud.sso.captcha.model.common.ResponseModel;
import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;

import java.util.Properties;

/**
 * decs:验证码服务接口
 *
 * @author zhimin.ma
 * @date 2021/1/1
 */
public interface CaptchaService {
    /**
     * 配置初始化
     *
     */
    void init(Properties config);

    /**
     * 获取验证码
     *
     * @param captchaVO
     * @return
     */
    CaptchaVO get(CaptchaVO captchaVO);

    /**
     * 核对验证码(前端)
     *
     * @param captchaVO
     * @return
     */
    CaptchaVO check(CaptchaVO captchaVO);

    /**
     * 二次校验验证码(后端)
     *
     * @param captchaVO
     * @return
     */
    CaptchaVO verification(CaptchaVO captchaVO);

    /***
     * 验证码类型
     * 通过java SPI机制，接入方可自定义实现类，实现新的验证类型
     * @return
     */
    String captchaType();

}

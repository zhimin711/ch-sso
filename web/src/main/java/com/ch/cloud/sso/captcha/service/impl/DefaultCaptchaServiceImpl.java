package com.ch.cloud.sso.captcha.service.impl;

import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.ch.cloud.sso.captcha.service.CaptchaService;
import com.ch.e.PubError;
import com.ch.e.Assert;

import java.util.Properties;

/**
 * decs:默认验证码.
 *
 * @author zhimin.ma
 * @since 2021/1/1
 */
public class DefaultCaptchaServiceImpl extends AbstractCaptchaService {

    @Override
    public String captchaType() {
        return "default";
    }

    @Override
    public void init(Properties config) {
        super.init(config);
    }

    private CaptchaService getService(String captchaType) {
        return CaptchaServiceFactory.instances.get(captchaType);
    }

    @Override
    public CaptchaVO get(CaptchaVO captchaVO) {
        Assert.notNull(captchaVO,  PubError.NON_NULL);
        Assert.notEmpty(captchaVO.getCaptchaType(), PubError.ARGS,"验证码类型");
        return getService(captchaVO.getCaptchaType()).get(captchaVO);
    }

    @Override
    public CaptchaVO check(CaptchaVO captchaVO) {
        Assert.notNull(captchaVO,  PubError.NON_NULL);
        Assert.notEmpty(captchaVO.getToken(), PubError.ARGS,"验证码");
        return getService(captchaVO.getCaptchaType()).check(captchaVO);
    }

}

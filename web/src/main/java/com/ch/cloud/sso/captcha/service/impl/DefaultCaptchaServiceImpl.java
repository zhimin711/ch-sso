package com.ch.cloud.sso.captcha.service.impl;

import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.ch.cloud.sso.captcha.service.CaptchaService;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;

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
        AssertUtils.isTrue(captchaVO == null || CommonUtils.isEmpty(captchaVO.getCaptchaType()), PubError.ARGS);
        return getService(captchaVO.getCaptchaType()).get(captchaVO);
    }

    @Override
    public CaptchaVO check(CaptchaVO captchaVO) {
        AssertUtils.isTrue(captchaVO == null || CommonUtils.isEmpty(captchaVO.getToken()), PubError.ARGS);
        return getService(captchaVO.getCaptchaType()).check(captchaVO);
    }

}

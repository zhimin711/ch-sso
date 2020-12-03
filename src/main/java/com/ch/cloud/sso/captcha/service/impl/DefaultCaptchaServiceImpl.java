package com.ch.cloud.sso.captcha.service.impl;

import com.ch.cloud.sso.captcha.model.common.RepCodeEnum;
import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.ch.cloud.sso.captcha.service.CaptchaService;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.ch.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * decs:默认验证码.
 *
 * @author zhimin.ma
 * @date 2021/1/1
 */
public class DefaultCaptchaServiceImpl extends AbstractCaptchaService {

    private static Logger logger = LoggerFactory.getLogger(DefaultCaptchaServiceImpl.class);

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
        if (captchaVO == null || CommonUtils.isEmpty(captchaVO.getCaptchaType())) {
            ExceptionUtils._throw(PubError.ARGS);
        }
        return getService(captchaVO.getCaptchaType()).get(captchaVO);
    }

    @Override
    public CaptchaVO check(CaptchaVO captchaVO) {
        if (captchaVO == null || CommonUtils.isEmptyOr(captchaVO.getCaptchaType(), captchaVO.getToken())) {
            ExceptionUtils._throw(PubError.ARGS);
        }
        return getService(captchaVO.getCaptchaType()).check(captchaVO);
    }

    @Override
    public CaptchaVO verification(CaptchaVO captchaVO) {
        if (captchaVO == null || CommonUtils.isEmptyOr(captchaVO.getCaptchaVerification())) {
            ExceptionUtils._throw(PubError.ARGS);
        }
        try {
            String codeKey = String.format(REDIS_SECOND_CAPTCHA_KEY, captchaVO.getCaptchaVerification());
            if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
                ExceptionUtils._throw(PubError.ARGS, RepCodeEnum.API_CAPTCHA_INVALID.getDesc());
            }
            //二次校验取值后，即刻失效
            CaptchaServiceFactory.getCache(cacheType).delete(codeKey);
        } catch (Exception e) {
            logger.error("验证码坐标解析失败", e);
            ExceptionUtils._throw(PubError.NOT_);
        }
        return new CaptchaVO();
    }

}

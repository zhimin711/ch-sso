package com.ch.cloud.sso.captcha.config;

import com.ch.cloud.sso.captcha.cache.CaptchaCacheService;
import com.ch.cloud.sso.captcha.properties.CaptchaProperties;
import com.ch.cloud.sso.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * decs:存储策略自动配置.
 *
 * @author zhimin.ma
 * @date 2021/1/1
 */
@Configuration
public class CaptchaStorageAutoConfiguration {

    @Bean(name = "captchaCacheService")
    public CaptchaCacheService captchaCacheService(CaptchaProperties captchaProperties){
        //缓存类型redis/local/....
        return CaptchaServiceFactory.getCache(captchaProperties.getCacheType().name());
    }
}

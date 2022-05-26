package com.ch.cloud.sso.captcha.config;

import com.ch.cloud.sso.captcha.properties.CaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * decs:验证码初始化
 *
 * @author zhimin.ma
 * @since 2021/1/1
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
@ComponentScan("com.ch.cloud.sso.captcha")
@Import({CaptchaServiceAutoConfiguration.class, CaptchaStorageAutoConfiguration.class})
public class CaptchaAutoConfiguration {
}

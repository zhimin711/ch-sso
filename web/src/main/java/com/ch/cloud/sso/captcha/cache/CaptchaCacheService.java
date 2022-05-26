/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.ch.cloud.sso.captcha.cache;

import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;

/**
 * 验证码缓存新接口
 *
 * @author zhimin.ma
 * @since 2020-08-21
 */
public interface CaptchaCacheService {

    void set(String key, CaptchaVO captchaVO);

    boolean exists(String key);

    void delete(String key);

    CaptchaVO get(String key);

    /**
     * 缓存类型-local/redis/caffeine/..
     * 通过java SPI机制，接入方可自定义实现类
     *
     * @return 缓存类型
     */
    String type();
}

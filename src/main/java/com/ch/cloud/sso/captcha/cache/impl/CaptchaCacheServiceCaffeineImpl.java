package com.ch.cloud.sso.captcha.cache.impl;

import com.ch.cloud.sso.captcha.cache.CaptchaCacheService;
import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.ch.cloud.sso.captcha.util.CaffeineCacheUtil;

/**
 * desc:Caffeine缓存
 * <p>
 * 对于分布式部署的应用，我们建议应用自己实现CaptchaCacheService，比如用Redis，参考service/spring-boot代码示例。
 * 如果应用是单点的，也没有使用redis，那默认使用内存。
 * 内存缓存只适合单节点部署的应用，否则验证码生产与验证在节点之间信息不同步，导致失败。
 *
 * @author zhimin.ma
 * @date 2020/12/15 12:46 PM
 */
public class CaptchaCacheServiceCaffeineImpl implements CaptchaCacheService {


    @Override
    public void set(String key, CaptchaVO value) {
        CaffeineCacheUtil.set(key, value);
    }

    @Override
    public boolean exists(String key) {
        return CaffeineCacheUtil.exists(key);
    }

    @Override
    public void delete(String key) {
        CaffeineCacheUtil.delete(key);
    }

    @Override
    public CaptchaVO get(String key) {
        return CaffeineCacheUtil.get(key);
    }

    @Override
    public String type() {
        return "caffeine";
    }
}

package com.ch.cloud.sso.captcha.cache.impl;

import com.ch.cloud.sso.captcha.cache.CaptchaCacheService;
import com.ch.cloud.sso.captcha.util.CacheUtil;
import com.google.auto.service.AutoService;

/**
 * 对于分布式部署的应用，我们建议应用自己实现CaptchaCacheService，比如用Redis，参考service/spring-boot代码示例。
 * 如果应用是单点的，也没有使用redis，那默认使用内存。
 * 内存缓存只适合单节点部署的应用，否则验证码生产与验证在节点之间信息不同步，导致失败。
 * @Title: 默认使用内存当缓存
 * @author lide1202@hotmail.com
 * @date 2020-05-12
 */
@AutoService(CaptchaCacheService.class)
public class CaptchaCacheServiceMemImpl implements CaptchaCacheService {
    @Override
    public void set(String key, String value, long expiresInSeconds) {

        CacheUtil.set(key, value, expiresInSeconds);
    }

    @Override
    public boolean exists(String key) {
        return CacheUtil.exists(key);
    }

    @Override
    public void delete(String key) {
        CacheUtil.delete(key);
    }

    @Override
    public String get(String key) {
        return CacheUtil.get(key);
    }

    @Override
    public String type() {
        return "local";
    }
}

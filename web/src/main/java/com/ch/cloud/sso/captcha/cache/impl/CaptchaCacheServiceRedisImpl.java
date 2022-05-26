package com.ch.cloud.sso.captcha.cache.impl;

import com.alibaba.fastjson.JSON;
import com.ch.cloud.sso.captcha.cache.CaptchaCacheService;
import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * decs:使用redis缓存
 * <p>
 * 对于分布式部署的应用，我们建议应用自己实现CaptchaCacheService，比如用Redis，参考service/spring-boot代码示例。
 * 如果应用是单点的，也没有使用redis，那默认使用内存。
 * 内存缓存只适合单节点部署的应用，否则验证码生产与验证在节点之间信息不同步，导致失败。
 *
 * @author zhimin.ma
 * @since 2019/9/9
 */
public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService {


    @Value("${captcha.timing.clear:180}")
    private long expiresInSeconds;

    @Override
    public String type() {
        return "redis";
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void set(String key, CaptchaVO value) {
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        Boolean e = stringRedisTemplate.hasKey(key);
        return e != null && e;
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public CaptchaVO get(String key) {
        return JSON.parseObject(stringRedisTemplate.opsForValue().get(key), CaptchaVO.class);
    }
}

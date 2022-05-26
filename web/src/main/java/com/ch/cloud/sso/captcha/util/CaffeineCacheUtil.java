package com.ch.cloud.sso.captcha.util;

import com.ch.cloud.sso.captcha.model.vo.CaptchaVO;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * decs:Caffeine
 *
 * @author 01370603
 * @since 2020/12/4
 */
@Slf4j
public class CaffeineCacheUtil {

    private static LoadingCache<String, CaptchaVO> CAPTCHA;

    /**
     * 初始化
     *
     * @param cacheMaxSize  缓存最大个数
     * @param expireSeconds 过期缓存时间
     */
    public static void init(long cacheMaxSize, long expireSeconds) {
        CAPTCHA = Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.MINUTES)//在最后一次写入缓存后开始计时，在指定的时间后过期。
                .build(key -> null); // 验证码信息缓存
    }

    /**
     * 缓存刷新,清除过期数据
     */
    public static void refresh(String key) {
        log.info("Caffeine 缓存刷新 {}", key);
        CAPTCHA.refresh(key);
    }


    public static void set(String key, CaptchaVO value) {
        CAPTCHA.put(key, value);
    }

    public static void delete(String key) {
        CAPTCHA.invalidate(key);
    }

    public static boolean exists(String key) {
        return get(key) != null;
    }


    public static CaptchaVO get(String key) {
        return CAPTCHA.get(key);
    }

    /**
     * 删除所有缓存
     */
    public static void clear() {
        log.info("have clean all key !");
        CAPTCHA.cleanUp();
    }
}

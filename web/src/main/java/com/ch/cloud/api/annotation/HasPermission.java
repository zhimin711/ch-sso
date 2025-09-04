package com.ch.cloud.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于标记需要权限校验的Controller方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasPermission {
    
    /**
     * 权限代码
     */
    String value();
    
    /**
     * 权限描述
     */
    String description() default "";
} 
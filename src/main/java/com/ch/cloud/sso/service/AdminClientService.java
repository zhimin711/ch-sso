package com.ch.cloud.sso.service;

import com.ch.cloud.sso.service.impl.AdminClientServiceImpl;
import com.ch.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * desc:用户微服务
 *
 * @author zhimin
 * @date 2019/4/15 12:41 PM
 */

@FeignClient(name = "ch-admin", fallback = AdminClientServiceImpl.class)
public interface AdminClientService {

    @GetMapping("user/{username}/info")
    Result<?> findUserByUsername(@PathVariable("username") String username);


    @GetMapping("user/{id}/role")
    Result<?> findRoleByUserId(@PathVariable("id") Long userId);

    @GetMapping("user/{id}/permission")
    Result<?> findPermissionByUserId(@PathVariable("id") Long userId);
}


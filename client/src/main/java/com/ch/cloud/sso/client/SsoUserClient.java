package com.ch.cloud.sso.client;

import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhimin
 */
@FeignClient(name = "${feign.client.sso:ch-sso}", contextId = "ssoUserClient", path = "/fc/user")
public interface SsoUserClient {
    
    
    @GetMapping("info")
    Result<UserInfo> info(@RequestParam("token") String token);
    
    
}

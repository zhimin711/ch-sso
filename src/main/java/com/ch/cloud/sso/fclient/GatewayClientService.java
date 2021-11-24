package com.ch.cloud.sso.fclient;

import com.ch.Constants;
import com.ch.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * desc:网关微服务
 *
 * @author zhimin
 * @date 2021/11/15 12:41 PM
 */
@FeignClient("ch-gateway")
public interface GatewayClientService {

    @GetMapping("/clean/user")
    Result<Boolean> cleanUser(@RequestHeader(Constants.X_TOKEN) String token);
}


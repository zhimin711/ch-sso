package com.ch.cloud.sso.client;

import com.ch.Constants;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${feign.client.sso:ch-sso}", contextId = "ssoLoginClient", path = "/fc/login/token")
public interface SsoLoginClient {

    @GetMapping("info")
    Result<UserInfo> info(@RequestHeader(Constants.X_TOKEN) String token);


}

package com.ch.cloud.sso.client;

import com.ch.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${feign.client.sso:ch-sso}", contextId = "ssoPasswordClient", path = "/fc/password")
public interface SsoPasswordClient {

    @PostMapping("encrypt")
    Result<String> encrypt(@RequestParam("str") String password);

    @PostMapping("matchEncrypt")
    Result<Boolean> matchEncrypt(@RequestParam("str") String password, @RequestParam("encodedStr") String encodedStr);

}

package com.ch.cloud.sso.controller;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @date 2019/4/14 2:08 PM
 */
@RestController
public class KeyGenController {

    @NacosInjected
    private NamingService namingService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //    @GetMapping("serviceInstances")
    public List<Instance> getInstances(@RequestParam String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }

    @PostMapping("encrypt")
    public Result<String> encrypt(@RequestParam String str) {
        return ResultUtils.wrapFail(() -> passwordEncoder.encode(str));
    }

    @PostMapping("matchEncrypt")
    public Result<Boolean> matchEncrypt(@RequestParam String str, @RequestParam String encodedStr) {
        return ResultUtils.wrapFail(() -> passwordEncoder.matches(str, encodedStr));
    }
}

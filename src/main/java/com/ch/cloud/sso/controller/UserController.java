package com.ch.cloud.sso.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * desc:
 *
 * @author zhimin
 * @date 2018/12/21 10:40 PM
 */
@RestController
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 资源服务器提供的受保护接口
     *
     * @param principal
     * @return
     */
    @GetMapping("user")
    public Principal user(Principal principal) {
        logger.debug("授权用户信息: {}", principal.toString());
        return principal;
    }

}

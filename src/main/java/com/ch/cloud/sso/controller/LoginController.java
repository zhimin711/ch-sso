package com.ch.cloud.sso.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * desc:
 *
 * @author zhimin
 * @date 2018/12/21 10:40 PM
 */
@RestController
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //
    @GetMapping("login")
    public ModelAndView index() {
        return new ModelAndView("login");
    }

}

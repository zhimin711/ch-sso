package com.ch.cloud.sso.client;

import com.ch.result.Result;
import com.ch.result.ResultUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * desc:
 *
 * @author zhimin
 * @since 2019/4/14 2:08 PM
 */
@RestController
@Log4j2
@RequestMapping("/fc/password")
public class PasswordClientController implements SsoPasswordClient {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("encrypt")
    public Result<String> encrypt(@RequestParam String str) {
        return ResultUtils.wrapFail(() -> passwordEncoder.encode(str));
    }
    
    @PostMapping("matchEncrypt")
    public Result<Boolean> matchEncrypt(@RequestParam String str, @RequestParam String encodedStr) {
        return ResultUtils.wrapFail(() -> passwordEncoder.matches(str, encodedStr));
    }
}

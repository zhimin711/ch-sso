package com.ch.cloud.sso.biz.manager.impl;

import com.ch.cloud.sso.biz.manager.UserManager;
import com.ch.cloud.sso.biz.tools.TokenCacheTool;
import com.ch.cloud.sso.security.CustomUserDetails;
import com.ch.cloud.upms.client.UpmsPermissionClient;
import com.ch.cloud.upms.client.UpmsRoleClient;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.e.Assert;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * desc: UserManagerImpl
 * </p>
 *
 * @author zhimin
 * @since 2025/7/4 09:02
 */
@Service
public class UserManagerImpl implements UserManager {

    @Autowired
    private UserDetailsService userDetailsService;

    @Resource
    private UpmsUserClient upmsUserClientService;

    @Resource
    private UpmsRoleClient upmsRoleClientService;

    @Resource
    private UpmsPermissionClient upmsPermissionClientService;

    // 如果在WebSecurityConfigurerAdapter中，没有重新，这里就会报注入失败的异常
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenCacheTool tokenCacheTool;

    @Override
    public CustomUserDetails login(String username, String password) {
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(upToken);
        } catch (BadCredentialsException e) {
            ExUtils.throwError(PubError.USERNAME_OR_PASSWORD, e);
        }
        Assert.notNull(authentication, PubError.NOT_AUTH, "登录失败！");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @Override
    public String generateAuthCode(String refreshToken, String url) {
        return tokenCacheTool.generateAuthCode(refreshToken, url);
    }

    @Override
    public String exchangeAuthCode(String authCode) {
        String username = tokenCacheTool.consumeAuthCode(authCode);
        return username;
    }
}

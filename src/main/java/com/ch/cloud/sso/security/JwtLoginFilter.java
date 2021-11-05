package com.ch.cloud.sso.security;

import com.ch.Constants;
import com.ch.cloud.client.dto.LoginUserDto;
import com.ch.cloud.sso.fclient.UpmsClientService;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * desc:
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/11/4
 */
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private JwtTokenTool jwtTokenTool;

    private UserDetailsService userDetailsService;

    public JwtLoginFilter(JwtTokenTool jwtTokenTool, UserDetailsService userDetailsService) {
        this.jwtTokenTool = jwtTokenTool;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //获取token
        String token = request.getHeader(Constants.X_TOKEN);
        if (CommonUtils.isNotEmpty(token) && !jwtTokenTool.isTokenExpired(token)) {

            UserInfo user = jwtTokenTool.getUserInfoFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), userDetails.getPassword());
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;
        }
        return super.attemptAuthentication(request, response);
    }
}

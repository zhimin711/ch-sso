package com.ch.cloud.sso.security;

import com.ch.Constants;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.biz.tools.TokenTool;
import com.ch.utils.CommonUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    private TokenTool tokenTool;

    private UserDetailsService userDetailsService;

    public JwtLoginFilter(TokenTool tokenTool, UserDetailsService userDetailsService) {
        this.tokenTool = tokenTool;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //获取token
        String token = request.getHeader(Constants.X_TOKEN);
        if (CommonUtils.isNotEmpty(token) && !tokenTool.isTokenExpired(token)) {

            UserInfo user = tokenTool.getUserInfoFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), userDetails.getPassword());
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;
        }
        return super.attemptAuthentication(request, response);
    }
}

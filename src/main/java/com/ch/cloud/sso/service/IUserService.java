package com.ch.cloud.sso.service;

import com.ch.cloud.client.dto.UserDto;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/9/9
 */
public interface IUserService {
    UserDto findByUsername(String username) throws UsernameNotFoundException;

    String login(String username, String password);

    String refreshToken(String token);

    /**
     * 校验TOKEN返回用户名
     *
     * @param token TOKEN
     * @return 用户名
     */
    String validate(String token);
}

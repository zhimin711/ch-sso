package com.ch.cloud.sso.service;

import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.pojo.UserVo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2019/9/9
 */
public interface IUserService {
    UserDto findByUsername(String username) throws UsernameNotFoundException;

    UserVo findUserInfo(String username, Long roleId);

    TokenVo login(String username, String password);

    void refreshToken(TokenVo tokenVo);

    /**
     * 校验TOKEN返回用户名
     *
     * @param token TOKEN
     * @return 用户名
     */
    String validate(String token);

    UserInfo extractToken(String token);
}

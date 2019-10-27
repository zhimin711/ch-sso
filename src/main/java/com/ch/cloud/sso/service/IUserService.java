package com.ch.cloud.sso.service;

import com.ch.cloud.client.dto.PermissionDto;
import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.pojo.RoleVo;
import com.ch.cloud.sso.pojo.UserVo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/9/9
 */
public interface IUserService {
    UserDto findByUsername(String username) throws UsernameNotFoundException;

    UserVo findUserInfo(String username, Long roleId);

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

package com.ch.cloud.sso.biz.service;

import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.biz.pojo.UserPermissionVo;
import com.ch.cloud.sso.biz.pojo.UserVo;
import com.ch.cloud.upms.dto.UserDto;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2019/9/9
 */
public interface IUserService {

    UserVo findUserInfo(String username);

    void refreshToken(TokenVo tokenVo);

    /**
     * 校验TOKEN返回用户名
     *
     * @param token TOKEN
     * @return 用户名
     */
    String validate(String token);

    UserInfo extractToken(String token);

    UserPermissionVo findPermission(UserInfo userInfo);
}

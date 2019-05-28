package com.ch.cloud.sso.service.impl;

import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.service.UpmsClientService;
import com.ch.result.Result;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @date 2019/4/15 12:46 PM
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {


    @Resource
    private UpmsClientService upmsClientService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Result<UserDto> res = upmsClientService.findUserByUsername(username);
        if (res.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        boolean enabled = true; // 可用性 :true:可用 false:不可用
        boolean accountNonExpired = true; // 过期性 :true:没过期 false:过期
        boolean credentialsNonExpired = true; // 有效性 :true:凭证有效 false:凭证无效
        boolean accountNonLocked = true; // 锁定性 :true:未锁定 false:已锁定

        UserDto user = res.get();
        String password = user.getPassword();
        Long id = user.getId();
        Result<String> res2 = upmsClientService.findRoleByUserId(id);
        List<String> roles = (List<String>) res2.getRows();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (!res2.isEmpty()) {
            roles.forEach(r -> {
                authorities.add(new SimpleGrantedAuthority(r));
                //todo get role permissions
            });
        }
        return new User(username, password, authorities);
    }
}

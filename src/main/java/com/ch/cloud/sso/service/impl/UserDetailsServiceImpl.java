package com.ch.cloud.sso.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.sso.service.UpmsClientService;
import com.ch.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @date 2019/4/15 12:46 PM
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {


    @Autowired
    private UpmsClientService upmsClientService;

    @SuppressWarnings("unchecked")
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Result<?> res = upmsClientService.findUserByUsername(username);
        if (res.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        boolean enabled = true; // 可用性 :true:可用 false:不可用
        boolean accountNonExpired = true; // 过期性 :true:没过期 false:过期
        boolean credentialsNonExpired = true; // 有效性 :true:凭证有效 false:凭证无效
        boolean accountNonLocked = true; // 锁定性 :true:未锁定 false:已锁定

        JSONObject user = new JSONObject((Map<String, Object>) res.get());
//        Map<String, Object> map = (Map<String, Object>) res.get();
        String password = user.getString("password");
        Long id = user.getLong("id");
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Result<?> res2 = upmsClientService.findRoleByUserId(id);
        List<Map<String, String>> roles = (List<Map<String, String>>) res2.getRows();
        roles.forEach(r -> {
            authorities.add(new SimpleGrantedAuthority(r.get("code")));
            //todo get role permissions
        });

        return new User(username, password, authorities);
    }
}

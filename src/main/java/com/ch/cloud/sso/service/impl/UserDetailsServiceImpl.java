package com.ch.cloud.sso.service.impl;

import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.cli.UpmsClientService;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserDetailsServiceImpl implements UserDetailsService, IUserService {


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

    @Override
    public UserDto findByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("用户名不可以为空!");
        }
        Result<UserDto> res = upmsClientService.findUserByUsername(username);
        if (res.isEmpty()) {
            throw new UsernameNotFoundException("用户名不存在!");
        }
//        log.info("SysUserServiceImpl......... {}", sysUser);
        return res.get();
    }

    //    @Override
    public UserVo findUserInfo(String username) {


        /**
         * 获取用户信息
         */
        UserDto sysUser = findByUsername(username);
        /**
         * 获取当前用户的所有角色
         */
        Result<String> res2 = upmsClientService.findRoleByUserId(sysUser.getId());

        /**
         * 在这里我的想法是，构建一个按钮权限列表
         * 再构建一个菜单权限列表
         * 这样的我们在前端的写的时候，就不用解析的很麻烦了
         * 因为权限表是一张表，在这里解析好了以后，
         * 相当前端少做一点工作，当然这也可以放到前端去解析权限列表
         */
//        Set<ButtonVo> buttonVos = new HashSet<>();
//        Set<MenuVo> menuVos = new HashSet<>();

//        sysRoles.forEach(role -> {
////            log.info("role: {}", role.getDescribe());
//            role.getPermissions().forEach(permission -> {
//                if (permission.getType().toLowerCase().equals("button")) {
//                    /*
//                     * 如果权限是按钮，就添加到按钮里面
//                     * */
//                    buttonVos.add(new ButtonVo(permission.getPid(), permission.getResources(), permission.getTitle()));
//                }
//                if (permission.getType().toLowerCase().equals("menu")) {
//                    /*
//                     * 如果权限是菜单，就添加到菜单里面
//                     * */
//                    menuVos.add(new MenuVo(permission.getPid(), permission.getFather(), permission.getIcon(), permission.getResources(), permission.getTitle()));
//                }
//            });
//        });

        /**
         * 注意这个类 TreeBuilder。因为的vue router是以递归的形式呈现菜单
         * 所以我们需要把菜单跟vue router 的格式一一对应 而按钮是不需要的
         */
//        SysUserVo sysUserVo =
//                new SysUserVo(sysUser.getUid(), sysUser.getAvatar(),
//                        sysUser.getNickname(), sysUser.getUsername(),
//                        sysUser.getMail(), sysUser.getAddTime(),
//                        sysUser.getRoles(), buttonVos, TreeBuilder.findRoots(menuVos));
        return new UserVo();
    }

    // 如果在WebSecurityConfigurerAdapter中，没有重新，这里就会报注入失败的异常
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenTool jwtTokenTool;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(upToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtTokenTool.generateToken(userDetails);
    }


    @Override
    public String refreshToken(String oldToken) {
        if (!jwtTokenTool.isTokenExpired(oldToken)) {
            return jwtTokenTool.refreshToken(oldToken);
        }
        return "error";
    }

    @Override
    public String validate(String token) {

        String username = jwtTokenTool.getUsernameFromToken(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 通过用户名 获取用户的信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 验证token和用户是否匹配
            if (jwtTokenTool.validateToken(token, userDetails)) return username;
        }
        return null;
    }

}

package com.ch.cloud.sso.service.impl;

import com.ch.cloud.client.dto.PermissionDto;
import com.ch.cloud.client.dto.RoleDto;
import com.ch.cloud.client.dto.UserDto;
import com.ch.cloud.sso.cli.UpmsClientService;
import com.ch.cloud.sso.pojo.BtnVo;
import com.ch.cloud.sso.pojo.MenuVo;
import com.ch.cloud.sso.pojo.RoleVo;
import com.ch.cloud.sso.pojo.UserVo;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import com.ch.utils.ExceptionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
        Result<String> res2 = upmsClientService.findRoleCodeByUserId(id);
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

    @Override
    public UserVo findUserInfo(String username, Long roleId) {
        /**
         * 获取用户信息
         */
        UserDto user = findByUsername(username);

        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        /**
         * 获取当前用户的所有角色
         */
        Result<RoleDto> res2 = upmsClientService.findRoleByUserId(user.getId());

        if (res2.isEmpty()) {
            return userVo;
        }

        List<Long> roleIds = Lists.newArrayList();
        List<RoleDto> roleList = Lists.newArrayList();
        List<RoleVo> roleVos = res2.getRows().stream().map(role -> {
            roleIds.add(role.getId());
            if (CommonUtils.isEquals(roleId, role.getId())) {
                roleList.add(role);
            }
            return new RoleVo(role.getId(), role.getCode(), role.getName());
        }).collect(Collectors.toList());


        if (roleId != null && roleId > 0 && !roleIds.contains(roleId)) {
            throw ExceptionUtils.create(PubError.NOT_EXISTS, "用户角色无效失败！");
        }

        userVo.setRoleList(roleVos);

        /**
         * 获取当前用户的角色菜单\权限
         */
        Result<PermissionDto> res3;
        Result<PermissionDto> res4;
        if ((roleList.isEmpty() && "SUPER_ADMIN".equals(res2.get().getCode())) || (!roleList.isEmpty() && "SUPER_ADMIN".equals(roleList.get(0).getCode()))) {
            res3 = upmsClientService.findMenuByRoleId(0L);
            res4 = upmsClientService.findPermissionByRoleId(0L);
        } else if (roleId != null && roleId > 0) {
            res3 = upmsClientService.findMenuByRoleId(roleId);
            res4 = upmsClientService.findPermissionByRoleId(roleId);
        } else {
            res3 = upmsClientService.findMenuByRoleId(roleVos.get(0).getId());
            res4 = upmsClientService.findPermissionByRoleId(roleVos.get(0).getId());
        }
        if (res3.isEmpty()) {
            return userVo;
        }
        /**
         * 构建一个菜单权限列表
         * 这样的我们在前端的写的时候，就不用解析的很麻烦了
         */
        Set<MenuVo> menuVos = Sets.newHashSet();
        if (!res3.isEmpty()) {
            Map<String, List<PermissionDto>> pidMap = res3.getRows().stream().collect(Collectors.groupingBy(PermissionDto::getPid));
            List<PermissionDto> topList = pidMap.get("0");
            topList.sort(Comparator.comparing(PermissionDto::getSort));
            topList.forEach(r -> {
                MenuVo menuVo = assemblyMenu(r, pidMap);
                menuVos.add(menuVo);
            });
        }
        /**
         * 构建一个按钮权限列表
         */
        Set<BtnVo> buttonVos = Sets.newHashSet();
        if (!res4.isEmpty()) {
            res4.getRows().forEach(permission -> {
                /*
                 * 如果权限是菜单，就添加到菜单里面
                 * */
                /*
                 * 如果权限是按钮，就添加到按钮里面
                 * */
                buttonVos.add(new BtnVo(permission.getPid(), permission.getCode(), permission.getName()));
            });
        }
        /**
         * 注意这个类 TreeBuilder。因为的vue router是以递归的形式呈现菜单
         * 所以我们需要把菜单跟vue router 的格式一一对应 而按钮是不需要的
         * sysUser.getUid(), sysUser.getAvatar(),
         * sysUser.getNickname(), sysUser.getUsername(),
         * sysUser.getMail(), sysUser.getAddTime(),
         * sysUser.getRoles(), buttonVos, TreeBuilder.findRoots(menuVos)
         */
        userVo.setMenuList(menuVos);
        userVo.setBtnList(buttonVos);
        return userVo;
    }

    private MenuVo assemblyMenu(PermissionDto permission, Map<String, List<PermissionDto>> pidMap) {
        MenuVo vo = new MenuVo(permission.getPid(), permission.getIcon(), permission.getCode(), permission.getName());
        vo.setType(permission.getType());
        vo.setUrl(permission.getUrl());
        if ("1".equals(permission.getType()) && pidMap.get(permission.getId().toString()) != null) {
            List<MenuVo> menuVos = pidMap.get(permission.getId().toString()).stream().map(e -> assemblyMenu(e, pidMap)).collect(Collectors.toList());
            vo.setChildren(menuVos);
        }
        return vo;
    }

    // 如果在WebSecurityConfigurerAdapter中，没有重新，这里就会报注入失败的异常
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenTool jwtTokenTool;


    @Override
    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(upToken);
        } catch (BadCredentialsException e) {
            throw ExceptionUtils.create(PubError.USERNAME_OR_PASSWORD, e);
        }
        if (authentication == null) {
            throw ExceptionUtils.create(PubError.NOT_AUTH, "登录失败！");
        }
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
        if (CommonUtils.isEmpty(token)) return null;
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

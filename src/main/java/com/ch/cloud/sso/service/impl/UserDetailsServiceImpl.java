package com.ch.cloud.sso.service.impl;

import com.ch.Num;
import com.ch.StatusS;
import com.ch.cloud.client.dto.*;
import com.ch.cloud.sso.fclient.UpmsClientService;
import com.ch.cloud.sso.pojo.*;
import com.ch.cloud.sso.security.CustomUserDetails;
import com.ch.cloud.sso.service.IUserService;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import com.ch.utils.StringUtilsV2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
@Slf4j
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService, IUserService {

    @Resource
    private UpmsClientService upmsClientService;

    // 如果在WebSecurityConfigurerAdapter中，没有重新，这里就会报注入失败的异常
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenTool jwtTokenTool;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Result<LoginUserDto> res = upmsClientService.findUserByUsername(username);
        if (res.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
//        boolean enabled = true; // 可用性 :true:可用 false:不可用
//        boolean accountNonExpired = true; // 过期性 :true:没过期 false:过期
//        boolean credentialsNonExpired = true; // 有效性 :true:凭证有效 false:凭证无效
//        boolean accountNonLocked = true; // 锁定性 :true:未锁定 false:已锁定

        LoginUserDto user = res.get();
        String password = user.getPassword();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority(user.getUsername()));
        String secret = jwtTokenTool.generateSecret(password);
        return new CustomUserDetails(username, password, authorities, secret);
    }

    @Override
    public UserDto findByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("用户名不可以为空!");
        }
        Result<UserDto> res = upmsClientService.findInfo2(username);
        if (res.isEmpty()) {
            throw new UsernameNotFoundException("用户名不存在!");
        }
//        log.info("SysUserServiceImpl......... {}", sysUser);
        return res.get();
    }

    @Override
    public UserVo findUserInfo(String username) {
        /**
         * 获取用户信息
         */
        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("用户名不可以为空!");
        }
        Result<UserDto> res = upmsClientService.findInfo2(username);
        if (res.isEmpty()) {
            throw new UsernameNotFoundException("用户名不存在!");
        }
//        log.info("SysUserServiceImpl......... {}", sysUser);
        UserDto user = res.get();

        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);

        if (CommonUtils.isEmpty(user.getRoleId())) {
            ExceptionUtils._throw(PubError.NOT_EXISTS, "用户没有角色或角色已失效！");
        }
        return userVo;
    }

    private MenuVo assemblyMenu(PermissionDto permission, Map<String, List<PermissionDto>> pidMap) {
        MenuVo vo = new MenuVo(permission.getParentId(), permission.getIcon(), permission.getCode(), permission.getName());
        vo.setType(permission.getType());
        vo.setUrl(permission.getUrl());
        vo.setRedirect(permission.getRedirect());
        vo.setSort(permission.getSort());
//        vo.setHidden(permission.isHidden());
        vo.setHidden(CommonUtils.isEquals(permission.getIsShow(), StatusS.DISABLED));
        String pid = CommonUtils.isEquals(Num.S0, permission.getParentId()) ? permission.getId().toString() : StringUtilsV2.linkStr(",", permission.getParentId(), permission.getId().toString());
        if (/*"1".equals(permission.getType()) && */pidMap.get(pid) != null) {
            List<MenuVo> menuVos = pidMap.get(pid).stream().map(e -> assemblyMenu(e, pidMap))
                    .sorted(Comparator.comparing(MenuVo::getSort)).collect(Collectors.toList());
            vo.setChildren(menuVos);
        }
        return vo;
    }

    @Override
    public TokenVo login(String username, String password) {
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
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setRoleId(userDetails.getRoleId());
        userInfo.setTenantId(userDetails.getTenantId());
        String token = jwtTokenTool.generateToken(userInfo, userDetails.getSecret());
        String refreshToken = jwtTokenTool.generateRefreshToken(userInfo, userDetails.getSecret());

        return new TokenVo(token, refreshToken, userInfo.getExpireAt());
    }

    private RoleVo findRoleByUsername(String username) {
        Result<RoleDto> res = upmsClientService.findRoleByUsername(username);
        if (res.isEmpty()) {
            return null;
        }
        RoleDto dto = res.get();
        return new RoleVo(dto.getId(), dto.getCode(), dto.getName());
    }


    @Override
    public void refreshToken(TokenVo tokenVo) {
        jwtTokenTool.refreshToken(tokenVo);
    }

    @Override
    public String validate(String token) {
        if (CommonUtils.isEmpty(token)) return null;
        String username = jwtTokenTool.getUsernameFromToken(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 通过用户名 获取用户的信息
            UserDetails userDetails = loadUserByUsername(username);
            // 验证token和用户是否匹配
            if (jwtTokenTool.validateToken(token, userDetails)) return username;
        }
        return null;
    }

    @Override
    public UserInfo extractToken(String token) {
        return jwtTokenTool.getUserInfoFromToken(token);
    }

    @Override
    public UserPermissionVo findPermission(UserInfo user) {
        UserPermissionVo userPermissionVo = new UserPermissionVo();
        /*
         * 获取当前用户的所有角色
         */
        Result<RoleDto> res2 = upmsClientService.findRolesByUserId(user.getUserId());
        if (res2.isEmpty()) {
            return userPermissionVo;
        }
        List<Long> roleIds = Lists.newArrayList();
        List<RoleVo> roleVos = res2.getRows().stream().map(role -> {
            roleIds.add(role.getId());
            return new RoleVo(role.getId(), role.getCode(), role.getName());
        }).collect(Collectors.toList());


        if (!roleIds.contains(user.getRoleId())) {
            ExceptionUtils._throw(PubError.NOT_EXISTS, "用户角色无效！");
        }
        userPermissionVo.setRoleList(roleVos);

        /*
         * 获取当前用户的角色菜单\权限
         */
        Result<PermissionDto> res3 = upmsClientService.findMenusByRoleId(user.getRoleId());
        Result<PermissionDto> res4 = upmsClientService.findPermissionsByRoleId(user.getRoleId());
        if (res3.isEmpty()) {
            return userPermissionVo;
        }
        /*
         * 构建一个菜单权限列表
         * 这样的我们在前端的写的时候，就不用解析的很麻烦了
         */
        List<MenuVo> menuVos = Lists.newArrayList();
        if (!res3.isEmpty()) {
            Map<String, List<PermissionDto>> pidMap = res3.getRows().stream().collect(Collectors.groupingBy(PermissionDto::getParentId));
            List<PermissionDto> topList = pidMap.get("0");
            if (topList != null && !topList.isEmpty()) {
                topList.sort(Comparator.comparing(PermissionDto::getSort));
                topList.forEach(r -> {
                    MenuVo menuVo = assemblyMenu(r, pidMap);
                    menuVos.add(menuVo);
                });
            }
        }
        /*
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
                buttonVos.add(new BtnVo(permission.getParentId(), permission.getCode(), permission.getName()));
            });
            res3.getRows().forEach(permission -> {
                //hidden menu add to btn permission
                if (CommonUtils.isEquals(permission.getType(), Num.S4)) {
                    buttonVos.add(new BtnVo(permission.getParentId(), permission.getCode(), permission.getName()));
                }
            });
        }
        /*
         * 注意这个类 TreeBuilder。因为的vue router是以递归的形式呈现菜单
         * 所以我们需要把菜单跟vue router 的格式一一对应 而按钮是不需要的
         * sysUser.getUid(), sysUser.getAvatar(),
         * sysUser.getNickname(), sysUser.getUsername(),
         * sysUser.getMail(), sysUser.getAddTime(),
         * sysUser.getRoles(), buttonVos, TreeBuilder.findRoots(menuVos)
         */
        userPermissionVo.setMenuList(menuVos);
        userPermissionVo.setBtnList(buttonVos);

        Result<TenantDto> res5 = upmsClientService.findTenantsByUserId(user.getUsername());
        if (!res5.isEmpty()) {
            userPermissionVo.setTenantList(res5.getRows());
        }
        return userPermissionVo;
    }

}

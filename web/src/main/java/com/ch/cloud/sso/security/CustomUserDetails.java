package com.ch.cloud.sso.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * <p>
 * desc:
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/11/1
 */
@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    /**
     * 角色Id
     */
    private Long roleId;
    /**
     * 租户Id
     */
    private Long tenantId;
    /**
     * 安全密钥
     */
    private final String secret;


    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String secret) {
        super(username, password, authorities);
        this.secret = secret;
    }

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long roleId, Long tenantId, String secret) {
        super(username, password, authorities);
        this.roleId = roleId;
        this.tenantId = tenantId;
        this.secret = secret;
    }
    
}

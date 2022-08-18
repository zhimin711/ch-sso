package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户Token信息
 *
 * @author zhimin.ma
 */
@Data
@NoArgsConstructor
public class UserInfo {

    /**
     * 用户名
     */
    private String username;
    /**
     * 用户ID
     */
    private String   userId;
    /**
     * 角色Id
     */
    private Long   roleId;
    /**
     * 租户ID
     */
    private Long   tenantId;
    /**
     * 过期时间
     */
    private Long   expireAt;
}

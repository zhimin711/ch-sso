package com.ch.cloud.sso.dto;

import com.ch.cloud.sso.pojo.UserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/8/14 20:33
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TokenCache extends UserInfo {

    private String password;

    /**
     * user client ip
     */
    private String clientIp;

    /**
     * user agent
     */
    private String userAgent;

    /**
     *  host
     */
    private String host;
    
    /**
     * 引用来源
     */
    private String referer;
}

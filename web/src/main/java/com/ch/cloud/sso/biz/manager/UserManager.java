package com.ch.cloud.sso.biz.manager;

import com.ch.cloud.sso.security.CustomUserDetails;

/**
 * <p>
 * desc: UserManager
 * </p>
 *
 * @author zhimin
 * @since 2025/7/4 09:01
 */
public interface UserManager {

    CustomUserDetails login(String username, String password);

    String generateAuthCode(String username, String url);

    String exchangeAuthCode(String authCode);
}

package com.ch.cloud.sso.biz.manager;

import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.pojo.UserInfo;

/**
 * <p>
 * desc: TokenManager - Token管理器接口
 * </p>
 *
 * @author zhimin
 * @since 2025/7/4 08:53
 */
public interface TokenManager {

    /**
     * 生成Token
     *
     * @param user   用户信息
     * @param secret 密钥
     * @return TokenVo对象
     */
    TokenVo generateToken(UserInfo user, String secret);

    /**
     * 验证Token
     *
     * @param token 访问Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的TokenVo对象
     */
    TokenVo refreshToken(String refreshToken);

    /**
     * 续期刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 是否续期成功
     */
    boolean renewRefreshToken(String refreshToken);

    /**
     * 获取用户信息
     *
     * @param token 访问Token
     * @return 用户信息
     */
    UserInfo getUserInfo(String token);

    /**
     * 删除Token
     *
     * @param token 访问Token
     */
    void deleteToken(String token);

    /**
     * 删除用户的所有Token
     *
     * @param username 用户名
     */
    void deleteUserTokens(String username);
}

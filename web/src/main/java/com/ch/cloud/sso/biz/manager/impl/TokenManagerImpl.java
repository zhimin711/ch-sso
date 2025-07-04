package com.ch.cloud.sso.biz.manager.impl;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.ch.cloud.sso.biz.manager.TokenManager;
import com.ch.cloud.sso.biz.pojo.TokenVo;
import com.ch.cloud.sso.biz.tools.TokenCacheTool;
import com.ch.cloud.sso.dto.RefreshTokenDTO;
import com.ch.cloud.sso.dto.TokenDTO;
import com.ch.cloud.sso.pojo.UserInfo;
import com.ch.cloud.sso.props.JwtProperties;
import com.ch.cloud.sso.utils.JwtUtil;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.DateUtils;
import com.ch.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * desc: TokenManagerImpl - Token管理器实现类
 * </p>
 *
 * @author zhimin
 * @since 2025/7/4 08:54
 */
@Service
@Slf4j
public class TokenManagerImpl implements TokenManager {
    
    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private TokenCacheTool tokenCacheTool;
    
    /**
     * 生成Token
     *
     * @param user   用户信息
     * @param secret 密钥
     * @return TokenVo对象
     */
    @Override
    public TokenVo generateToken(UserInfo user, String secret) {
        Date current = DateUtils.current();
        
        // 构造JWT claims
        Map<String, Object> claims = new HashMap<>(5);
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        claims.put("tenantId", user.getTenantId());
        claims.put("created", new Date());
        
        // 计算访问token和刷新token的过期时间
        Date accessExpired = DateUtils.addSeconds(current, (int) jwtProperties.getTokenExpired().getSeconds());
        Date refreshExpired = DateUtils.addSeconds(current, (int) jwtProperties.getRefreshTokenExpired().getSeconds());
        
        // 生成JWT字符串
        String jwtToken = JwtUtil.generateToken(claims, refreshExpired, secret);
        
        // 生成访问token和刷新token
        String accessToken = UuidUtils.generateUuid();
        String refreshToken = EncryptUtils.md5(jwtToken);
        
        log.info("生成Token - 当前时间: {}, 访问Token过期时间: {}", DateUtils.format(current),
                DateUtils.format(accessExpired));
        
        // 克隆用户信息到TokenCache对象
        TokenDTO tokenDTO = BeanUtilsV2.clone(user, TokenDTO.class);
        tokenDTO.setExpireAt(accessExpired.getTime());
        tokenDTO.setPassword(secret);
        
        // 使用TokenRedisUtil保存访问Token
        tokenCacheTool.saveAccessToken(accessToken, tokenDTO, jwtProperties.getTokenExpired().getSeconds());
        
        // 构造RefreshTokenCache对象
        RefreshTokenDTO refreshTokenDTO = RefreshTokenDTO.build(jwtToken, secret, user.getUsername(), refreshExpired);
        
        // 使用TokenRedisUtil保存刷新Token
        tokenCacheTool.saveRefreshToken(refreshToken, refreshTokenDTO, jwtProperties.getRefreshTokenExpired().getSeconds());
        
        return new TokenVo(accessToken, refreshToken, accessExpired.getTime());
    }
    
    /**
     * 验证Token
     *
     * @param token 访问Token
     * @return 是否有效
     */
    @Override
    public boolean validateToken(String token) {
        return tokenCacheTool.hasAccessToken(token);
    }
    
    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的TokenVo对象
     */
    @Override
    public TokenVo refreshToken(String refreshToken) {
        // 获取刷新Token缓存信息
        RefreshTokenDTO refreshTokenDTO = tokenCacheTool.getRefreshTokenCache(refreshToken);
        if (refreshTokenDTO == null) {
            log.warn("刷新Token不存在: {}", refreshToken);
            return null;
        }
        
        // 验证刷新Token是否过期
        if (refreshTokenDTO.getExpired().before(new Date())) {
            log.warn("刷新Token已过期: {}", refreshToken);
            return null;
        }
        
        // 获取用户信息
        String username = refreshTokenDTO.getUsername();
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        
        // 生成新的Token
        return generateToken(userInfo, refreshTokenDTO.getSecret());
    }
    
    /**
     * 续期刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 是否续期成功
     */
    @Override
    public boolean renewRefreshToken(String refreshToken) {
        return tokenCacheTool.renewToken(refreshToken, jwtProperties.getRefreshTokenExpired().getSeconds());
    }
    
    /**
     * 获取用户信息
     *
     * @param token 访问Token
     * @return 用户信息
     */
    @Override
    public UserInfo getUserInfo(String token) {
        TokenDTO tokenDTO = tokenCacheTool.getTokenCache(token);
        if (tokenDTO != null) {
            return BeanUtilsV2.clone(tokenDTO, UserInfo.class);
        }
        return null;
    }
    
    /**
     * 删除Token
     *
     * @param token 访问Token
     */
    @Override
    public void deleteToken(String token) {
        tokenCacheTool.deleteAccessToken(token);
    }
    
    /**
     * 删除用户的所有Token
     *
     * @param username 用户名
     */
    @Override
    public void deleteUserTokens(String username) {
        tokenCacheTool.deleteUserTokens(username);
    }
}

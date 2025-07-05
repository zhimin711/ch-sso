package com.ch.cloud.sso.biz.tools;

import com.alibaba.fastjson2.JSON;
import com.ch.cloud.sso.captcha.util.RandomUtils;
import com.ch.cloud.sso.dto.RefreshTokenDTO;
import com.ch.cloud.sso.dto.TokenDTO;
import com.ch.e.Assert;
import com.ch.e.PubError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Token Redis工具类
 * 封装RedisTemplate，提供Token与用户信息映射的管理功能
 * 支持多平台Token管理和授权码机制
 *
 * @author zhimin
 * @since 2025/1/1
 */
@Slf4j
@Component
public class TokenCacheTool {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis Key前缀常量
    public static final String TOKEN_CACHE_PREFIX = "sso:token:";
    public static final String TOKEN_USER_PREFIX = "sso:token_user:";
    public static final String USER_TOKEN_PREFIX = "sso:user_token:";

    public static final String REFRESH_TOKEN_CACHE_PREFIX = "sso:refresh_token:";
    public static final String REFRESH_TOKEN_USER_PREFIX = "sso:refresh_token_user:";
    public static final String USER_REFRESH_TOKEN_PREFIX = "sso:user_refresh_token:";

    public static final String REFRESH_TOKEN_ACCESS_PREFIX = "sso:refresh_token_access:";

    // 多平台Token管理相关前缀
    public static final String USER_PLATFORM_TOKENS_PREFIX = "sso:user_platform_tokens:";
    public static final String PLATFORM_USER_TOKENS_PREFIX = "sso:platform_user_tokens:";
    public static final String AUTH_CODE_PREFIX = "sso:auth_code:";
    public static final String USER_AUTH_CODES_PREFIX = "sso:user_auth_codes:";

    // 平台标识常量
    public static final String PLATFORM_AUTH = "auth";      // 用户权限管理平台
    public static final String PLATFORM_API = "api";        // 接口管理平台

    /**
     * 保存访问Token与用户信息映射（支持多平台）
     *
     * @param token      访问Token
     * @param tokenDTO   Token缓存信息
     * @param expireTime 过期时间（秒）
     */
    public void saveAccessToken(String token, TokenDTO tokenDTO, long expireTime) {
        try {
            String tokenKey = TOKEN_CACHE_PREFIX + token;
            String tokenUserKey = TOKEN_USER_PREFIX + token;
            String userKey = USER_TOKEN_PREFIX + tokenDTO.getUsername();

            // 保存Token详细信息
            redisTemplate.opsForValue().set(tokenKey, tokenDTO, expireTime, TimeUnit.SECONDS);
            // 保存Token与用户的映射关系
            stringRedisTemplate.opsForValue().set(tokenUserKey, tokenDTO.getUsername(), expireTime, TimeUnit.SECONDS);

            // 保存用户与Token的映射关系,多平台登录用集合保存
//            stringRedisTemplate.opsForValue().set(userKey, token, expireTime, TimeUnit.SECONDS);
            stringRedisTemplate.opsForSet().add(userKey, token);
            stringRedisTemplate.expire(userKey, expireTime, TimeUnit.SECONDS);

            log.debug("保存访问Token成功: {}, 用户: {}, 平台: {}, 过期时间: {}秒", token, tokenDTO.getUsername(), tokenDTO.getReferer(), expireTime);
        } catch (Exception e) {
            log.error("保存访问Token失败: {}", token, e);
            throw new RuntimeException("保存访问Token失败", e);
        }
    }


    /**
     * 保存刷新Token与用户信息映射
     *
     * @param refreshToken    刷新Token
     * @param refreshTokenDTO 刷新Token缓存信息
     * @param expireTime      过期时间（秒）
     */
    public void saveRefreshToken(String refreshToken, RefreshTokenDTO refreshTokenDTO, long expireTime) {
        try {
            String refreshTokenKey = REFRESH_TOKEN_CACHE_PREFIX + refreshToken;
            String refreshTokenUserKey = REFRESH_TOKEN_USER_PREFIX + refreshToken;
            String userRefreshKey = USER_REFRESH_TOKEN_PREFIX + refreshTokenDTO.getUsername();

            // 保存刷新Token详细信息
            redisTemplate.opsForValue().set(refreshTokenKey, refreshTokenDTO, expireTime, TimeUnit.SECONDS);

            // 保存刷新Token与用户的映射关系
            stringRedisTemplate.opsForValue().set(refreshTokenUserKey, refreshTokenDTO.getUsername(), expireTime, TimeUnit.SECONDS);

            // 保存用户与刷新Token的映射关系
            stringRedisTemplate.opsForValue().set(userRefreshKey, refreshToken, expireTime, TimeUnit.SECONDS);

            log.debug("保存刷新Token成功: {}, 用户: {}, 过期时间: {}秒", refreshToken, refreshTokenDTO.getUsername(), expireTime);
        } catch (Exception e) {
            log.error("保存刷新Token失败: {}", refreshToken, e);
            throw new RuntimeException("保存刷新Token失败", e);
        }
    }

    public void saveRefreshTokenAccess(String refreshToken, String accessToken, long expireTime) {
        try {
            String refreshTokenAccessKey = REFRESH_TOKEN_ACCESS_PREFIX + refreshToken;

            // 保存刷新Token与访问Token的映射关系
            stringRedisTemplate.opsForValue().set(refreshTokenAccessKey, accessToken, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("保存刷新Token与访问Token映射关系失败: {}", refreshToken, e);
        }
    }

    /**
     * 生成授权码
     *
     * @param username 用户名
     * @return 授权码
     */
    public String generateAuthCode(String username, String url) {
        try {
            String authCode = RandomUtils.getRandomString(8);
            String authCodeKey = AUTH_CODE_PREFIX + authCode;
            String userAuthCodesKey = USER_AUTH_CODES_PREFIX + username;

            // 保存授权码
            stringRedisTemplate.opsForValue().set(authCodeKey, username, 60, TimeUnit.SECONDS);

            // 保存用户授权码列表
//            stringRedisTemplate.opsForSet().add(userAuthCodesKey, authCode);
            stringRedisTemplate.opsForHash().put(userAuthCodesKey, authCode, url);
            stringRedisTemplate.expire(userAuthCodesKey, 60, TimeUnit.SECONDS);

            log.debug("生成授权码成功: {}, 用户: {}, 目标平台: {}", authCode, username, url);
            return authCode;
        } catch (Exception e) {
            log.error("生成授权码失败: {}", username, e);
            throw new RuntimeException("生成授权码失败", e);
        }
    }

    /**
     * 验证授权码
     *
     * @param authCode 授权码
     * @return 授权码信息，如果无效返回null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> validateAuthCode(String authCode) {
        try {
            String authCodeKey = AUTH_CODE_PREFIX + authCode;
            Object value = redisTemplate.opsForValue().get(authCodeKey);

            if (value instanceof Map) {
                return (Map<String, Object>) value;
            } else if (value instanceof String) {
                return JSON.parseObject((String) value, Map.class);
            }
            return null;
        } catch (Exception e) {
            log.error("验证授权码失败: {}", authCode, e);
            return null;
        }
    }

    /**
     * 使用授权码并删除
     *
     * @param authCode 授权码
     * @return 授权码信息
     */
    public String consumeAuthCode(String authCode) {
        try {
            String authCodeKey = AUTH_CODE_PREFIX + authCode;
            String username = stringRedisTemplate.opsForValue().getAndDelete(authCodeKey);
            Assert.notEmpty(username, PubError.INVALID, "授权码无效或已过期");

            // 删除授权码
            String userAuthCodesKey = USER_AUTH_CODES_PREFIX + username;
            stringRedisTemplate.opsForSet().remove(userAuthCodesKey, authCode);
            String url = (String) stringRedisTemplate.opsForHash().get(userAuthCodesKey, authCode);
            stringRedisTemplate.opsForHash().delete(userAuthCodesKey, authCode);

            log.debug("使用授权码成功: {}, 用户: {}", authCode, username);
            return username;
        } catch (Exception e) {
            log.error("使用授权码失败: {}", authCode, e);
            return null;
        }
    }

    /**
     * 根据用户名和平台获取访问Token
     *
     * @param username 用户名
     * @param platform 平台标识
     * @return 访问Token，如果不存在返回null
     */
    public String getAccessTokenByUsernameAndPlatform(String username, String platform) {
        try {
            String userPlatformTokensKey = USER_PLATFORM_TOKENS_PREFIX + username + ":" + platform;
            return stringRedisTemplate.opsForValue().get(userPlatformTokensKey);
        } catch (Exception e) {
            log.error("根据用户名和平台获取访问Token失败: {}, {}", username, platform, e);
            return null;
        }
    }

    /**
     * 根据用户名和平台获取刷新Token
     *
     * @param username 用户名
     * @param platform 平台标识
     * @return 刷新Token，如果不存在返回null
     */
    public String getRefreshTokenByUsernameAndPlatform(String username, String platform) {
        try {
            String userPlatformRefreshKey = USER_PLATFORM_TOKENS_PREFIX + username + ":" + platform + ":refresh";
            return stringRedisTemplate.opsForValue().get(userPlatformRefreshKey);
        } catch (Exception e) {
            log.error("根据用户名和平台获取刷新Token失败: {}, {}", username, platform, e);
            return null;
        }
    }

    /**
     * 删除用户指定平台的所有Token
     *
     * @param username 用户名
     * @param platform 平台标识
     */
    public void deleteUserPlatformTokens(String username, String platform) {
        try {
            String accessToken = getAccessTokenByUsernameAndPlatform(username, platform);
            String refreshToken = getRefreshTokenByUsernameAndPlatform(username, platform);

            if (accessToken != null) {
                deleteAccessToken(accessToken);
            }
            if (refreshToken != null) {
                deleteRefreshToken(refreshToken);
            }

            // 删除平台相关的映射
            String userPlatformTokensKey = USER_PLATFORM_TOKENS_PREFIX + username + ":" + platform;
            String userPlatformRefreshKey = USER_PLATFORM_TOKENS_PREFIX + username + ":" + platform + ":refresh";
            String platformUserTokensKey = PLATFORM_USER_TOKENS_PREFIX + platform + ":" + username;

            stringRedisTemplate.delete(userPlatformTokensKey);
            stringRedisTemplate.delete(userPlatformRefreshKey);
            stringRedisTemplate.delete(platformUserTokensKey);

            log.debug("删除用户平台Token成功: {}, 平台: {}", username, platform);
        } catch (Exception e) {
            log.error("删除用户平台Token失败: {}, {}", username, platform, e);
        }
    }

    /**
     * 获取用户在所有平台的Token信息
     *
     * @param username 用户名
     * @return 平台Token映射
     */
    public Map<String, String> getUserAllPlatformTokens(String username) {
        try {
            Map<String, String> platformTokens = new HashMap<>();

            // 获取权限管理平台Token
            String authToken = getAccessTokenByUsernameAndPlatform(username, PLATFORM_AUTH);
            if (authToken != null) {
                platformTokens.put(PLATFORM_AUTH, authToken);
            }

            // 获取接口管理平台Token
            String apiToken = getAccessTokenByUsernameAndPlatform(username, PLATFORM_API);
            if (apiToken != null) {
                platformTokens.put(PLATFORM_API, apiToken);
            }

            return platformTokens;
        } catch (Exception e) {
            log.error("获取用户所有平台Token失败: {}", username, e);
            return new HashMap<>();
        }
    }

    /**
     * 同步用户Token到其他平台
     *
     * @param username       用户名
     * @param sourcePlatform 源平台
     * @param targetPlatform 目标平台
     * @param expireTime     过期时间（秒）
     * @return 是否同步成功
     */
    public boolean syncUserTokenToPlatform(String username, String sourcePlatform, String targetPlatform, long expireTime) {
        try {
            // 获取源平台的Token信息
            TokenDTO sourceTokenDTO = getTokenCache(getAccessTokenByUsernameAndPlatform(username, sourcePlatform));
            if (sourceTokenDTO == null) {
                log.warn("源平台Token不存在: {}, {}", username, sourcePlatform);
                return false;
            }

            // 生成目标平台的Token
            String targetToken = RandomUtils.getRandomString(32);
            TokenDTO targetTokenDTO = new TokenDTO();
            targetTokenDTO.setUsername(sourceTokenDTO.getUsername());
            targetTokenDTO.setUserId(sourceTokenDTO.getUserId());
            targetTokenDTO.setRoleId(sourceTokenDTO.getRoleId());
            targetTokenDTO.setTenantId(sourceTokenDTO.getTenantId());
            targetTokenDTO.setExpireAt(System.currentTimeMillis() + expireTime * 1000);
            targetTokenDTO.setPassword(sourceTokenDTO.getPassword());

            // 保存目标平台Token
            saveAccessToken(targetToken, targetTokenDTO, expireTime);

            log.debug("同步用户Token成功: {}, 源平台: {}, 目标平台: {}", username, sourcePlatform, targetPlatform);
            return true;
        } catch (Exception e) {
            log.error("同步用户Token失败: {}, {}, {}", username, sourcePlatform, targetPlatform, e);
            return false;
        }
    }


    /**
     * 根据访问Token获取用户信息
     *
     * @param token 访问Token
     * @return TokenCache对象，如果不存在返回null
     */
    public TokenDTO getTokenCache(String token) {
        try {
            String tokenKey = TOKEN_CACHE_PREFIX + token;
            Object value = redisTemplate.opsForValue().get(tokenKey);
            if (value instanceof TokenDTO) {
                return (TokenDTO) value;
            } else if (value instanceof String) {
                return JSON.parseObject((String) value, TokenDTO.class);
            }
            return null;
        } catch (Exception e) {
            log.error("获取Token缓存信息失败: {}", token, e);
            return null;
        }
    }

    /**
     * 根据刷新Token获取刷新Token缓存信息
     *
     * @param refreshToken 刷新Token
     * @return RefreshTokenCache对象，如果不存在返回null
     */
    public RefreshTokenDTO getRefreshTokenCache(String refreshToken) {
        try {
            String refreshTokenKey = REFRESH_TOKEN_CACHE_PREFIX + refreshToken;
            Object value = redisTemplate.opsForValue().get(refreshTokenKey);
            if (value instanceof RefreshTokenDTO) {
                return (RefreshTokenDTO) value;
            } else if (value instanceof String) {
                return JSON.parseObject((String) value, RefreshTokenDTO.class);
            }
            return null;
        } catch (Exception e) {
            log.error("获取刷新Token缓存信息失败: {}", refreshToken, e);
            return null;
        }
    }

    /**
     * 根据用户名获取访问Token
     *
     * @param username 用户名
     * @return 访问Token，如果不存在返回null
     */
    public String getAccessTokenByUsername(String username) {
        try {
            String userKey = USER_TOKEN_PREFIX + username;
            return stringRedisTemplate.opsForValue().get(userKey);
        } catch (Exception e) {
            log.error("根据用户名获取访问Token失败: {}", username, e);
            return null;
        }
    }

    /**
     * 根据用户名获取刷新Token
     *
     * @param username 用户名
     * @return 刷新Token，如果不存在返回null
     */
    public String getRefreshTokenByUsername(String username) {
        try {
            String userRefreshKey = USER_REFRESH_TOKEN_PREFIX + username;
            return stringRedisTemplate.opsForValue().get(userRefreshKey);
        } catch (Exception e) {
            log.error("根据用户名获取刷新Token失败: {}", username, e);
            return null;
        }
    }

    /**
     * 根据访问Token获取用户名
     *
     * @param token 访问Token
     * @return 用户名，如果不存在返回null
     */
    public String getUsernameByToken(String token) {
        try {
            String tokenUserKey = TOKEN_USER_PREFIX + token;
            return stringRedisTemplate.opsForValue().get(tokenUserKey);
        } catch (Exception e) {
            log.error("根据访问Token获取用户名失败: {}", token, e);
            return null;
        }
    }

    /**
     * 根据刷新Token获取用户名
     *
     * @param refreshToken 刷新Token
     * @return 用户名，如果不存在返回null
     */
    public String getUsernameByRefreshToken(String refreshToken) {
        try {
            String refreshTokenUserKey = REFRESH_TOKEN_USER_PREFIX + refreshToken;
            return stringRedisTemplate.opsForValue().get(refreshTokenUserKey);
        } catch (Exception e) {
            log.error("根据刷新Token获取用户名失败: {}", refreshToken, e);
            return null;
        }
    }

    /**
     * 检查访问Token是否存在
     *
     * @param token 访问Token
     * @return 是否存在
     */
    public boolean hasAccessToken(String token) {
        try {
            String tokenKey = TOKEN_CACHE_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
        } catch (Exception e) {
            log.error("检查访问Token是否存在失败: {}", token, e);
            return false;
        }
    }

    /**
     * 检查刷新Token是否存在
     *
     * @param refreshToken 刷新Token
     * @return 是否存在
     */
    public boolean hasRefreshToken(String refreshToken) {
        try {
            String refreshTokenKey = REFRESH_TOKEN_CACHE_PREFIX + refreshToken;
            return Boolean.TRUE.equals(redisTemplate.hasKey(refreshTokenKey));
        } catch (Exception e) {
            log.error("检查刷新Token是否存在失败: {}", refreshToken, e);
            return false;
        }
    }

    /**
     * 删除访问Token及其相关映射
     *
     * @param token 访问Token
     */
    public void deleteAccessToken(String token) {
        try {
            String username = getUsernameByToken(token);
            if (username != null) {
                String tokenKey = TOKEN_CACHE_PREFIX + token;
                String userKey = USER_TOKEN_PREFIX + username;
                String tokenUserKey = TOKEN_USER_PREFIX + token;

                redisTemplate.delete(tokenKey);
                stringRedisTemplate.delete(userKey);
                stringRedisTemplate.delete(tokenUserKey);

                log.debug("删除访问Token成功: {}, 用户: {}", token, username);
            }
        } catch (Exception e) {
            log.error("删除访问Token失败: {}", token, e);
        }
    }

    /**
     * 删除刷新Token及其相关映射
     *
     * @param refreshToken 刷新Token
     */
    public void deleteRefreshToken(String refreshToken) {
        try {
            String username = getUsernameByRefreshToken(refreshToken);
            if (username != null) {
                String refreshTokenKey = REFRESH_TOKEN_CACHE_PREFIX + refreshToken;
                String userRefreshKey = USER_REFRESH_TOKEN_PREFIX + username;
                String refreshTokenUserKey = REFRESH_TOKEN_USER_PREFIX + refreshToken;

                redisTemplate.delete(refreshTokenKey);
                stringRedisTemplate.delete(userRefreshKey);
                stringRedisTemplate.delete(refreshTokenUserKey);

                log.debug("删除刷新Token成功: {}, 用户: {}", refreshToken, username);
            }
        } catch (Exception e) {
            log.error("删除刷新Token失败: {}", refreshToken, e);
        }
    }

    /**
     * 删除用户的所有Token
     *
     * @param username 用户名
     */
    public void deleteUserTokens(String username) {
        try {
            String accessToken = getAccessTokenByUsername(username);
            String refreshToken = getRefreshTokenByUsername(username);

            if (accessToken != null) {
                deleteAccessToken(accessToken);
            }
            if (refreshToken != null) {
                deleteRefreshToken(refreshToken);
            }

            log.debug("删除用户所有Token成功: {}", username);
        } catch (Exception e) {
            log.error("删除用户所有Token失败: {}", username, e);
        }
    }

    /**
     * 续期访问Token
     *
     * @param token      访问Token
     * @param expireTime 新的过期时间（秒）
     * @return 是否续期成功
     */
    public boolean renewAccessToken(String token, long expireTime) {
        try {
            TokenDTO tokenDTO = getTokenCache(token);
            if (tokenDTO != null) {
                String tokenKey = TOKEN_CACHE_PREFIX + token;
                String userKey = USER_TOKEN_PREFIX + tokenDTO.getUsername();
                String tokenUserKey = TOKEN_USER_PREFIX + token;

                // 更新过期时间
                redisTemplate.expire(tokenKey, expireTime, TimeUnit.SECONDS);
                stringRedisTemplate.expire(userKey, expireTime, TimeUnit.SECONDS);
                stringRedisTemplate.expire(tokenUserKey, expireTime, TimeUnit.SECONDS);

                log.debug("续期访问Token成功: {}, 用户: {}, 新过期时间: {}秒", token, tokenDTO.getUsername(), expireTime);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("续期访问Token失败: {}", token, e);
            return false;
        }
    }

    /**
     * 续期刷新Token
     *
     * @param refreshToken 刷新Token
     * @param expireTime   新的过期时间（秒）
     * @return 是否续期成功
     */
    public boolean renewToken(String refreshToken, long expireTime) {
        try {
            RefreshTokenDTO refreshTokenDTO = getRefreshTokenCache(refreshToken);
            if (refreshTokenDTO != null) {
                String refreshTokenKey = REFRESH_TOKEN_CACHE_PREFIX + refreshToken;
                String userRefreshKey = USER_REFRESH_TOKEN_PREFIX + refreshTokenDTO.getUsername();
                String refreshTokenUserKey = REFRESH_TOKEN_USER_PREFIX + refreshToken;

                // 更新过期时间
                redisTemplate.expire(refreshTokenKey, expireTime, TimeUnit.SECONDS);
                stringRedisTemplate.expire(userRefreshKey, expireTime, TimeUnit.SECONDS);
                stringRedisTemplate.expire(refreshTokenUserKey, expireTime, TimeUnit.SECONDS);

                log.debug("续期刷新Token成功: {}, 用户: {}, 新过期时间: {}秒", refreshToken, refreshTokenDTO.getUsername(), expireTime);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("续期刷新Token失败: {}", refreshToken, e);
            return false;
        }
    }

    /**
     * 获取访问Token的剩余过期时间
     *
     * @param token 访问Token
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public long getAccessTokenExpireTime(String token) {
        try {
            String tokenKey = TOKEN_CACHE_PREFIX + token;
            Long expire = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取访问Token过期时间失败: {}", token, e);
            return -2;
        }
    }

    /**
     * 获取刷新Token的剩余过期时间
     *
     * @param refreshToken 刷新Token
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public long getRefreshTokenExpireTime(String refreshToken) {
        try {
            String refreshTokenKey = REFRESH_TOKEN_CACHE_PREFIX + refreshToken;
            Long expire = redisTemplate.getExpire(refreshTokenKey, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取刷新Token过期时间失败: {}", refreshToken, e);
            return -2;
        }
    }

    /**
     * 批量删除过期的Token
     * 注意：这个方法需要谨慎使用，可能会影响性能
     */
    public void cleanExpiredTokens() {
        try {
            // 这里可以实现批量清理逻辑
            // 由于Redis的SCAN操作比较复杂，这里只是提供一个框架
            log.info("开始清理过期的Token");
            // TODO: 实现具体的清理逻辑
            log.info("清理过期的Token完成");
        } catch (Exception e) {
            log.error("清理过期的Token失败", e);
        }
    }
}

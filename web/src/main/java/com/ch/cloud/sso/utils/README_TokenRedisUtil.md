# TokenRedisUtil 工具类使用说明

## 概述

`TokenRedisUtil` 是一个封装了 RedisTemplate 的工具类，专门用于管理 Token 与用户信息的映射关系。它提供了完整的 Token 生命周期管理功能，包括存储、获取、删除、续期等操作。

## 主要功能

### 1. Token 存储管理
- **保存访问Token**: `saveAccessToken(String token, TokenCache tokenDTO, long expireTime)`
- **保存刷新Token**: `saveRefreshToken(String refreshToken, RefreshTokenCache refreshTokenDTO, long expireTime)`

### 2. Token 查询功能
- **获取Token缓存信息**: `getTokenCache(String token)`
- **获取刷新Token缓存信息**: `getRefreshTokenCache(String refreshToken)`
- **根据用户名获取访问Token**: `getAccessTokenByUsername(String username)`
- **根据用户名获取刷新Token**: `getRefreshTokenByUsername(String username)`
- **根据Token获取用户名**: `getUsernameByToken(String token)`
- **根据刷新Token获取用户名**: `getUsernameByRefreshToken(String refreshToken)`

### 3. Token 验证功能
- **检查访问Token是否存在**: `hasAccessToken(String token)`
- **检查刷新Token是否存在**: `hasRefreshToken(String refreshToken)`

### 4. Token 删除功能
- **删除访问Token**: `deleteAccessToken(String token)`
- **删除刷新Token**: `deleteRefreshToken(String refreshToken)`
- **删除用户的所有Token**: `deleteUserTokens(String username)`

### 5. Token 续期功能
- **续期访问Token**: `renewAccessToken(String token, long expireTime)`
- **续期刷新Token**: `renewRefreshToken(String refreshToken, long expireTime)`

### 6. Token 过期时间管理
- **获取访问Token过期时间**: `getAccessTokenExpireTime(String token)`
- **获取刷新Token过期时间**: `getRefreshTokenExpireTime(String refreshToken)`

## Redis Key 设计

工具类使用以下 Key 前缀来组织数据：

```java
public static final String TOKEN_CACHE_PREFIX = "sso:token:";           // 访问Token详细信息
public static final String REFRESH_TOKEN_CACHE_PREFIX = "sso:refresh_token:"; // 刷新Token详细信息
public static final String USER_TOKEN_PREFIX = "sso:user_token:";       // 用户->访问Token映射
public static final String TOKEN_USER_PREFIX = "sso:token_user:";       // 访问Token->用户映射
public static final String USER_REFRESH_TOKEN_PREFIX = "sso:user_refresh_token:"; // 用户->刷新Token映射
public static final String REFRESH_TOKEN_USER_PREFIX = "sso:refresh_token_user:"; // 刷新Token->用户映射
```

## 使用示例

### 1. 在 TokenManager 中使用

```java
@Service
public class TokenManagerImpl implements TokenManager {
    
    @Autowired
    private TokenRedisUtil tokenRedisUtil;
    
    @Override
    public TokenVo generateToken(UserInfo user, String secret) {
        // 生成Token逻辑...
        
        // 保存访问Token
        tokenRedisUtil.saveAccessToken(accessToken, tokenDTO, expireTime);
        
        // 保存刷新Token
        tokenRedisUtil.saveRefreshToken(refreshToken, refreshTokenDTO, expireTime);
        
        return new TokenVo(accessToken, refreshToken, expireTime);
    }
    
    @Override
    public boolean validateToken(String token) {
        return tokenRedisUtil.hasAccessToken(token);
    }
    
    @Override
    public boolean renewRefreshToken(String refreshToken) {
        return tokenRedisUtil.renewRefreshToken(refreshToken, expireTime);
    }
}
```

### 2. 在 Controller 中使用

```java
@RestController
public class TokenController {
    
    @Autowired
    private TokenRedisUtil tokenRedisUtil;
    
    @GetMapping("/token/validate")
    public Result<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = tokenRedisUtil.hasAccessToken(token);
        return Result.success(isValid);
    }
    
    @PostMapping("/token/renew")
    public Result<Boolean> renewToken(@RequestParam String refreshToken) {
        boolean success = tokenRedisUtil.renewRefreshToken(refreshToken, 86400); // 24小时
        return Result.success(success);
    }
}
```

### 3. 直接使用工具类

```java
@Component
public class CustomTokenService {
    
    @Autowired
    private TokenRedisUtil tokenRedisUtil;
    
    public void processUserLogin(String username, String token) {
        // 检查用户是否已有Token
        String existingToken = tokenRedisUtil.getAccessTokenByUsername(username);
        if (existingToken != null) {
            // 删除旧Token
            tokenRedisUtil.deleteAccessToken(existingToken);
        }
        
        // 保存新Token
        TokenCache tokenDTO = new TokenCache();
        tokenDTO.setUsername(username);
        // 设置其他属性...
        
        tokenRedisUtil.saveAccessToken(token, tokenDTO, 3600); // 1小时过期
    }
    
    public UserInfo getUserInfo(String token) {
        TokenCache tokenDTO = tokenRedisUtil.getTokenCache(token);
        if (tokenDTO != null) {
            return convertToUserInfo(tokenDTO);
        }
        return null;
    }
}
```

## 配置要求

### 1. Redis 配置

确保在 `application.yml` 中配置了 Redis：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
    database: 1
    timeout: 60000
    jedis:
      pool:
        max-active: 9
        max-wait: -1
        max-idle: 9
        min-idle: 0
```

### 2. 依赖注入

工具类使用 `@Component` 注解，Spring Boot 会自动扫描并注入：

```java
@Autowired
private TokenRedisUtil tokenRedisUtil;
```

## 注意事项

1. **异常处理**: 所有方法都包含了异常处理，确保 Redis 连接问题不会影响应用稳定性。

2. **日志记录**: 工具类使用 SLF4J 记录详细的操作日志，便于调试和监控。

3. **数据一致性**: 删除操作会同时删除所有相关的映射关系，确保数据一致性。

4. **性能考虑**: 
   - 使用 Redis 的原子操作
   - 合理设置过期时间
   - 避免频繁的 Redis 操作

5. **安全性**: 
   - Token 信息在 Redis 中存储时使用适当的序列化方式
   - 敏感信息（如密码）应该加密存储

## 扩展功能

如果需要添加新的功能，可以在 `TokenRedisUtil` 类中添加新的方法，例如：

- 批量操作功能
- Token 统计功能
- 过期 Token 清理功能
- Token 使用频率统计

## 故障排查

### 常见问题

1. **Redis 连接失败**: 检查 Redis 配置和网络连接
2. **序列化问题**: 确保 TokenCache 和 RefreshTokenCache 类实现了序列化接口
3. **内存不足**: 监控 Redis 内存使用情况，及时清理过期数据

### 调试建议

1. 启用 DEBUG 日志级别查看详细操作信息
2. 使用 Redis 客户端工具查看实际存储的数据
3. 监控 Redis 性能指标 
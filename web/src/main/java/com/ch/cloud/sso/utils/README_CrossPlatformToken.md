# 跨平台Token管理解决方案

## 概述

本解决方案提供了完整的跨平台Token管理功能，支持用户权限管理平台和接口管理平台之间的Token共享和授权码跳转机制。

## 架构设计

### 平台架构
```
用户权限管理平台 (auth.example.com)  ←→  SSO服务  ←→  接口管理平台 (api.example.com)
     ↓                                    ↓                    ↓
   用户登录                              Token管理             无登录功能
   权限管理                             授权码生成             通过授权码访问
   生成授权码                           跨平台同步
```

### 核心组件

1. **TokenCacheTool**: 扩展的Token缓存工具类，支持多平台Token管理
2. **MultiPlatformTokenService**: 跨平台Token管理服务
3. **CrossPlatformTokenController**: 跨平台Token管理API接口
4. **前端演示页面**: 完整的业务流程演示

## 业务流程

### 1. 用户登录流程
```
1. 用户在权限管理平台登录
2. 系统生成权限管理平台Token
3. Token存储在Redis中，关联用户和平台信息
```

### 2. 跨平台跳转流程
```
1. 用户在权限管理平台点击"跳转到接口管理平台"
2. 权限管理平台调用API生成授权码
3. 用户被重定向到接口管理平台（携带授权码）
4. 接口管理平台使用授权码获取Token
5. 用户可以在接口管理平台正常使用
```

### 3. Token同步机制
```
1. 源平台Token有效时，可以同步到目标平台
2. 目标平台Token过期时，可以从源平台重新同步
3. 支持Token续期和刷新
```

## API接口说明

### 1. 授权码管理

#### 生成授权码
```http
POST /api/cross-platform/auth-code/generate
Content-Type: application/x-www-form-urlencoded

username=admin&sourcePlatform=auth&targetPlatform=api
```

**响应示例：**
```json
{
  "success": true,
  "data": "ABC123XY",
  "message": "授权码生成成功"
}
```

#### 使用授权码获取Token
```http
POST /api/cross-platform/auth-code/exchange
Content-Type: application/x-www-form-urlencoded

authCode=ABC123XY&targetPlatform=api
```

**响应示例：**
```json
{
  "success": true,
  "data": {
    "token": "api_token_123456",
    "userInfo": {
      "username": "admin",
      "userId": "1001",
      "roleId": 1,
      "tenantId": 1
    },
    "isNew": true
  }
}
```

#### 验证授权码
```http
GET /api/cross-platform/auth-code/validate?authCode=ABC123XY
```

### 2. Token管理

#### 刷新目标平台Token
```http
POST /api/cross-platform/token/refresh
Content-Type: application/x-www-form-urlencoded

username=admin&sourcePlatform=auth&targetPlatform=api
```

#### 获取用户跨平台信息
```http
GET /api/cross-platform/user/info?username=admin
```

#### 验证所有平台Token状态
```http
GET /api/cross-platform/user/validate?username=admin
```

#### 清理用户所有平台Token
```http
DELETE /api/cross-platform/user/clean?username=admin
```

## 使用示例

### 1. 权限管理平台集成

```javascript
// 用户登录后，生成跳转授权码
async function generateJumpAuthCode(username) {
    const response = await fetch('/api/cross-platform/auth-code/generate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${username}&sourcePlatform=auth&targetPlatform=api`
    });
    
    const result = await response.json();
    if (result.success) {
        // 重定向到接口管理平台
        window.location.href = `https://api.example.com?authCode=${result.data}`;
    }
}
```

### 2. 接口管理平台集成

```javascript
// 页面加载时检查授权码
async function checkAuthCode() {
    const urlParams = new URLSearchParams(window.location.search);
    const authCode = urlParams.get('authCode');
    
    if (authCode) {
        const response = await fetch('/api/cross-platform/auth-code/exchange', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `authCode=${authCode}&targetPlatform=api`
        });
        
        const result = await response.json();
        if (result.success) {
            // 保存Token到本地存储
            localStorage.setItem('apiToken', result.data.token);
            // 清除URL中的授权码
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }
}
```

### 3. Token验证中间件

```java
@Component
public class CrossPlatformTokenInterceptor implements HandlerInterceptor {
    
    @Autowired
    private TokenCacheTool tokenCacheTool;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            // 验证Token
            if (tokenCacheTool.hasAccessToken(token)) {
                TokenCache tokenDTO = tokenCacheTool.getTokenCache(token);
                if (tokenDTO != null) {
                    // 将用户信息设置到请求属性中
                    request.setAttribute("userInfo", tokenDTO);
                    return true;
                }
            }
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
```

## Redis数据结构

### 1. Token存储结构
```
sso:token:{token}                    -> TokenCache对象
sso:user_token:{username}            -> 用户访问Token
sso:token_user:{token}               -> Token对应的用户名
sso:user_platform_tokens:{username}:{platform} -> 用户平台Token
sso:platform_user_tokens:{platform}:{username} -> 平台用户Token
```

### 2. 授权码存储结构
```
sso:auth_code:{authCode}             -> 授权码信息
sso:user_auth_codes:{username}       -> 用户授权码集合
```

### 3. 刷新Token存储结构
```
sso:refresh_token:{refreshToken}     -> RefreshTokenCache对象
sso:user_refresh_token:{username}    -> 用户刷新Token
sso:refresh_token_user:{refreshToken} -> 刷新Token对应的用户名
sso:user_platform_tokens:{username}:{platform}:refresh -> 用户平台刷新Token
```

## 配置要求

### 1. Redis配置
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

### 2. JWT配置
```yaml
jwt:
  secret: your_jwt_secret
  tokenExpired: 30m
  refreshTokenExpired: 1d
```

### 3. 跨域配置
```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

## 安全考虑

### 1. 授权码安全
- 授权码有效期限制（5分钟）
- 一次性使用，使用后立即删除
- 随机生成，避免猜测

### 2. Token安全
- Token有效期控制
- 支持Token续期和刷新
- 支持Token撤销和清理

### 3. 权限控制
- 检查用户是否有权限访问目标平台
- 支持基于角色的权限控制
- 支持租户隔离

## 监控和日志

### 1. 关键日志点
- 授权码生成和使用
- Token同步和刷新
- 跨平台访问验证
- 异常情况记录

### 2. 监控指标
- 授权码生成频率
- Token同步成功率
- 跨平台访问成功率
- Redis连接状态

## 故障排查

### 1. 常见问题

#### 授权码无效
- 检查授权码是否过期
- 检查授权码是否已被使用
- 检查Redis连接状态

#### Token同步失败
- 检查源平台Token是否有效
- 检查用户权限
- 检查Redis存储空间

#### 跨平台访问失败
- 检查目标平台Token是否存在
- 检查Token是否过期
- 检查用户权限配置

### 2. 调试建议
- 启用DEBUG日志级别
- 使用Redis客户端查看数据
- 检查网络连接状态
- 验证配置参数

## 扩展功能

### 1. 支持更多平台
- 可以轻松扩展到更多平台
- 支持平台特定的权限控制
- 支持平台间的数据同步

### 2. 高级功能
- Token使用统计
- 用户会话管理
- 实时Token状态监控
- 自动Token清理

### 3. 性能优化
- Redis集群支持
- Token缓存优化
- 批量操作支持
- 异步处理机制

## 总结

本解决方案提供了完整的跨平台Token管理功能，支持：

1. **多平台Token管理**: 支持用户权限管理平台和接口管理平台
2. **授权码机制**: 安全的跨平台跳转机制
3. **Token同步**: 自动同步用户Token到不同平台
4. **权限控制**: 基于角色的跨平台访问控制
5. **监控管理**: 完整的监控和日志记录

通过这个解决方案，可以实现用户在不同平台间的无缝切换，同时保证安全性和可维护性。 
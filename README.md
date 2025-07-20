# ch-sso（朝华SingleSignOn）

#### 介绍
单点登录（SingleSignOn,SSO）是保障各业务系统的用户资源的安全 。  
各个业务系统获得的信息是私有的，用户能不能访问资源由SSO提供。  

单点登录，资源都在各个业务系统这边，不在SSO那一方。 用户在给SSO服务器提供了用户名密码后，作为业务系统并不知道这件事。 SSO随便给业务系统一个ST，那么业务系统是不能确定这个ST是用户伪造的，还是真的有效，所以要拿着这个ST去SSO服务器再问一下，这个用户给我的ST是否有效，是有效的我才能让这个用户访问

* 使用Spring Boot + Spring Security框架
* 使用Alibaba Nacos 为注册与配置中心
* 使用Jwt生成、刷新与验证token
* 使用Awt Image生成验证码
* 使用Redis 缓存Token与验证码
* 已集群Oauth2（默认未配置）

#### 软件架构
ch-sso是基于Spring Boot + Spring Security构建的企业级单点登录系统，核心架构包括：
- **认证授权**：基于OAuth2.0协议，支持密码模式、授权码模式
- **Token管理**：使用JWT生成访问令牌(Access Token)和刷新令牌(Refresh Token)，Redis缓存实现令牌存储与验证
- **服务治理**：集成Alibaba Nacos作为服务注册与配置中心
- **安全防护**：包含验证码(AWT生成)、滑动验证、密码加密存储等安全机制
- **集群支持**：支持分布式部署，默认提供集群化OAuth2配置(需手动开启)

架构流程图：
```
用户 → 业务系统 → SSO认证中心 → 生成Token → 业务系统验证Token → 资源访问
```


#### 安装教程

1. 修改配置文件（基于Wiki基础服务）  
（1） resources/config/application-local.yml  
修改Jwt配置
```yaml
server:
  port: 7000
logging:
  config: classpath:conf/logback-test.xml
  path: logs
  level:
    com.ch: debug
    com.alibaba.nacos.client: warn
###feign 默认关闭熔断，请看HystrixFeignConfiguration
feign:
  hystrix:
    enabled: false
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 60000
ribbon:
  ReadTimeout: 30000
  ConnectTimeout: 60000
  MaxAutoRetries: 0
  MaxAutoRetriesNextServer: 1
jwt:
  #jwt的加密字符串
  secret: eyJleHAiOjE1NDMyMDUyODUsInN1YiI6ImFkbWluIiwiY3Jl
  #jwt token有效时间（分钟）
  tokenExpired: 30m
  refreshTokenExpired: 1d
```
（2） resources/bootstrap.yml  
修改namespace与server-addr
```yaml
nacos:
  config:
    namespace: local
    server-addr: 192.168.199.194:8848
spring:
  application:
    name: ch-sso
  profiles:
    active: local
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.config.server-addr}
        namespace: ${nacos.config.namespace:}
      config:
        server-addr: ${nacos.config.server-addr}
        shared-dataids: ch-sso.yml
        namespace: ${nacos.config.namespace:}


```
2. 上传配置文件（application-local.yml,注:文件名要修改为"应用名称".yml（spring.application.name））到Nacos

3. 启动服务
~~~
#gradle工具命令启动：
gradle bootJar
#docker部署参考other目录deploy.md
~~~

#### 使用说明

### 核心API接口

#### 认证相关
- `GET /login` - 获取登录页面
- `POST /login/access` - 用户登录(参数: username, password, captcha)
- `GET /login/auth-code` - 获取手机验证码
- `GET /login/token/refresh` - 刷新令牌
- `GET /login/captcha` - 获取图形验证码
- `GET /login/slideCaptcha` - 获取滑动验证码

#### 用户相关
- `GET /user` - 获取用户列表
- `GET /user/info` - 获取当前用户信息
- `POST /user/permissions` - 获取用户权限
- `GET /user/auth-code` - 获取用户授权码

#### 令牌相关
- `GET /api/token/validate` - 验证令牌有效性
- `GET /api/token/user-info` - 获取令牌关联用户信息
- `GET /api/token/refresh-token` - 获取刷新令牌

#### 登出相关
- `GET /oauth/logout` - 用户登出
- `POST /logout/token` - 注销令牌

### 调用示例
**用户登录**:
```bash
curl -X POST http://localhost:7000/login/access \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456","captcha":"8a3d"}'
```

**验证令牌**:
```bash
curl -X GET http://localhost:7000/api/token/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

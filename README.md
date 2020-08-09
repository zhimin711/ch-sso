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
请参见Wiki文档 [传送门](https://gitee.com/ch-cloud/wiki)


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


1. 请参见Wiki文档 [传送门](https://gitee.com/ch-cloud/wiki)

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

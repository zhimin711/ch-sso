# ch-sso（朝华SingleSignOn）

#### 介绍
单点登录（SingleSignOn,SSO）是保障各业务系统的用户资源的安全 。  
各个业务系统获得的信息是，这个用户能不能访问我的资源。  

单点登录，资源都在各个业务系统这边，不在SSO那一方。 用户在给SSO服务器提供了用户名密码后，作为业务系统并不知道这件事。 SSO随便给业务系统一个ST，那么业务系统是不能确定这个ST是用户伪造的，还是真的有效，所以要拿着这个ST去SSO服务器再问一下，这个用户给我的ST是否有效，是有效的我才能让这个用户访问

* 使用Spring Boot + Spring Security框架
* 使用Alibaba Nacos 为注册与配置中心
* 使用Jwt生成、刷新与验证token
* 使用Awt Image生成验证码
* 使用Redis 缓存Token与验证码

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


1. swagger 
http://localhost:7000/auth/swagger-ui.html
2. xxxx
3. xxxx

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request


#### 码云特技

1. 使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2. 码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3. 你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4. [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5. 码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6. 码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)


[Spring核心组件详解（Bean、Context、Core）](https://blog.csdn.net/zlfprogram/article/details/75937935)
[Dubbo 问题](https://blog.csdn.net/Y0Q2T57s/article/details/83005376)
[Redis 问题](https://www.jianshu.com/p/36a646cef11a)
[Java NIO系列教程（一） Java NIO 概述](http://ifeve.com/overview/)  
[CentOS7 安装 MariaDB10.3](https://blog.csdn.net/KradMe/article/details/80763718)
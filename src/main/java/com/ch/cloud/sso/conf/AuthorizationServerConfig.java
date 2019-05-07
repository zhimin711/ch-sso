package com.ch.cloud.sso.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author 01370603  on 2017/6/9.
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private AuthenticationManager authenticationManager;
    //    @Autowired
//    private RedisConnectionFactory connectionFactory;
    @Resource
    private DataSource dataSource;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
//    @Resource
//    UserDetailsService userDetailsService;


    //    @Bean
    public TokenStore tokenStore() {
//        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setSigningKey("123");
//        return new JwtTokenStore(converter);
        //使用Redis缓存的tokenStore,令牌（Access Token）会保存到Redis
//        return new RedisTokenStore(connectionFactory);

        //这个是基于JDBC的实现tokenStore，令牌（Access Token）会保存到数据库
//        return new JdbcTokenStore(dataSource);
        //使用内存的tokenStore,令牌（Access Token）会保存到内存
        return new InMemoryTokenStore();
    }

//    @Override
//    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
//        //配置userDetailsService这样每次认证的时候会去检验用户是否锁定，有效等
//        endpoints.authenticationManager(authenticationManager)
//                .userDetailsService(userDetailsService)//若无，refresh_token会有UserDetailsService is required错误
//                .tokenStore(tokenStore());
//    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
//                .allowFormAuthenticationForClients()
        ;

    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {


//        logger.info("===============配置授权服务器开始...=========");
//        clients.inMemory()
//                .withClient("ChaoHua") // clientId, 可以类比为用户名
//                .secret(passwordEncoder.encode("secret")) // secret， 可以类比为密码
//                .authorizedGrantTypes("authorization_code", "password", "refresh_token")    // 授权类型，这里选择授权码
//                .scopes("read", "write") // 允许授权范围--这一项用于服务提供商区分提供哪些服务数据
////                .autoApprove(true) // 自动认证
//                .redirectUris("http://localhost:8083/blog/login")    // 认证成功重定向URL
//                .accessTokenValiditySeconds(10); // 超时时间，10s

        clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
        logger.info("===============配置授权服务器完成=========");
    }
}

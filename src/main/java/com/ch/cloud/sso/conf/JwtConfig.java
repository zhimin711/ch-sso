package com.ch.cloud.sso.conf;

import com.ch.cloud.sso.props.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/4
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
}

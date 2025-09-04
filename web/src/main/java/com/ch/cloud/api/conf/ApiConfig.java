package com.ch.cloud.api.conf;


import com.ch.cloud.api.props.ApiAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApiAuthProperties.class)
public class ApiConfig {

}

package com.ch.cloud.api.props;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "api.auth")
public class ApiAuthProperties {

    private Integer days = 7;

    private List<String> permissions;
}

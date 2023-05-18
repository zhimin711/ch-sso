package com.ch.cloud.sso.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * decs:
 *
 * @author 01370603
 * @since 2019/12/12
 */

@ConfigurationProperties("jwt")
@ConfigurationPropertiesBinding
public class JwtProperties {
    /**
     * ns for nanoseconds
     * us for microseconds
     * ms for milliseconds
     * s for seconds
     * m for minutes
     * h for hours
     * d for days
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration tokenExpired = Duration.ofSeconds(30);

    private Duration refreshTokenExpired = Duration.ofDays(7);

    private String secret;

    public Duration getTokenExpired() {
        return tokenExpired;
    }

    public void setTokenExpired(Duration tokenExpired) {
        this.tokenExpired = tokenExpired;
    }

    public Duration getRefreshTokenExpired() {
        return refreshTokenExpired;
    }

    public void setRefreshTokenExpired(Duration refreshTokenExpired) {
        this.refreshTokenExpired = refreshTokenExpired;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}

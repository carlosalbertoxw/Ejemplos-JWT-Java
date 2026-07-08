package com.carlosalbertoxw.jwt.api.config;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades "jwt.*" de application.yml, convertidas al JwtSettings del nucleo. */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        String audience,
        String secret,
        long accessMinutes,
        long refreshDays) {

    public JwtSettings toSettings() {
        return new JwtSettings(issuer, audience, secret, accessMinutes, refreshDays);
    }
}

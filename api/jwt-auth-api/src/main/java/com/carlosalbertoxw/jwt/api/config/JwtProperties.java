package com.carlosalbertoxw.jwt.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades "jwt.*" de application.yml. El secreto llega por la variable
 * JWT_SECRET y puede quedar vacio en desarrollo (ver CoreBeansConfig).
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        String audience,
        String secret,
        long accessMinutes,
        long refreshDays) {
}

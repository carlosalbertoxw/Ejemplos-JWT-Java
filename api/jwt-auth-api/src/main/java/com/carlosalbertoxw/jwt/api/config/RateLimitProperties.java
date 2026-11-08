package com.carlosalbertoxw.jwt.api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades "rate-limit.*": fallos permitidos por cuenta y por IP dentro de la ventana.
 *
 * @param accountMaxAttempts fallos por cuenta (frena la fuerza bruta contra un usuario)
 * @param ipMaxAttempts      fallos por IP (frena el "password spraying" contra muchas cuentas)
 * @param window             ventana deslizante
 */
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(int accountMaxAttempts, int ipMaxAttempts, Duration window) {
}

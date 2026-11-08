package com.carlosalbertoxw.jwt.api.web;

import com.carlosalbertoxw.jwt.core.ratelimit.RateLimiter;

/**
 * Limites de intentos de los endpoints de autenticacion.
 *
 * @param account limite estricto por cuenta
 * @param ip      limite mas holgado por IP, compartido por todas las cuentas
 */
public record AuthRateLimits(RateLimiter account, RateLimiter ip) {

    /** Lanza TooManyAttemptsException si la clave ya agoto sus intentos. */
    static void check(RateLimiter limiter, String key) {
        if (limiter.tooManyAttempts(key)) {
            throw new TooManyAttemptsException(limiter.retryAfterSeconds(key));
        }
    }
}

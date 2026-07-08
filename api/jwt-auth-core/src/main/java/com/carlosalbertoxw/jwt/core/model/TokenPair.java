package com.carlosalbertoxw.jwt.core.model;

import java.time.Instant;

/**
 * Par de tokens devuelto al iniciar sesion o refrescar.
 */
public record TokenPair(String accessToken, String refreshToken, Instant accessExpiresAt) {
}

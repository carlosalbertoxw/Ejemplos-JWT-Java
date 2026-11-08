package com.carlosalbertoxw.jwt.core.model;

/**
 * Par de tokens devuelto al iniciar sesion o refrescar.
 *
 * @param accessToken      JWT de corta vida
 * @param refreshToken     token opaco de larga vida
 * @param expiresInSeconds segundos de vida del access token
 */
public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
}

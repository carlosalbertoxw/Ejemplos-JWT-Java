package com.carlosalbertoxw.jwt.core.config;

/**
 * Parametros de configuracion del emisor de tokens.
 *
 * @param issuer          emisor esperado (claim "iss")
 * @param audience        audiencia esperada (claim "aud")
 * @param secret          secreto para firmar (HMAC). Debe tener >= 32 bytes.
 * @param accessMinutes   minutos de vida del access token (corto)
 * @param refreshDays     dias de vida del refresh token (largo)
 */
public record JwtSettings(
        String issuer,
        String audience,
        String secret,
        long accessMinutes,
        long refreshDays) {

    /** Longitud minima de la clave HMAC-SHA256 (RFC 7518: 256 bits). */
    public static final int MIN_SECRET_BYTES = 32;
}

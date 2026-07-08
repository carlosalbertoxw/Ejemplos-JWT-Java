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

    /** Valores por defecto pensados para desarrollo/demostracion. */
    public static JwtSettings defaults() {
        return new JwtSettings(
                "jwt-ejemplo-java",
                "jwt-ejemplo-clientes",
                "clave-secreta-de-ejemplo-para-firmar-jwt-cambia-esto-en-produccion",
                15,
                7);
    }
}

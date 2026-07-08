package com.carlosalbertoxw.jwt.client.dto;

import java.time.Instant;

/** Respuesta de autenticacion recibida desde la API. */
public record AuthResult(String accessToken, String refreshToken, Instant accessExpiresAt) {
}

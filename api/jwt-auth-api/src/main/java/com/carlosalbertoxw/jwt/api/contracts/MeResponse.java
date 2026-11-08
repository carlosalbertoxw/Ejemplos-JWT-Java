package com.carlosalbertoxw.jwt.api.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta de GET /api/auth/me: los claims del access token recibido. */
public record MeResponse(
        String id,
        String username,
        String role,
        @JsonProperty("token_expires_at") long tokenExpiresAt) {
}

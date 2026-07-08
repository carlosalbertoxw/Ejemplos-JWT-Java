package com.carlosalbertoxw.jwt.api.contracts;

import com.carlosalbertoxw.jwt.core.model.TokenPair;
import java.time.Instant;

/** Respuesta con el par de tokens emitido. */
public record AuthResponse(String accessToken, String refreshToken, Instant accessExpiresAt) {

    public static AuthResponse from(TokenPair pair) {
        return new AuthResponse(pair.accessToken(), pair.refreshToken(), pair.accessExpiresAt());
    }
}

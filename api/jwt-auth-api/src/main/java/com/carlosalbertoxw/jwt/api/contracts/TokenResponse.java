package com.carlosalbertoxw.jwt.api.contracts;

import com.carlosalbertoxw.jwt.core.model.TokenPair;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta de login y refresh (contrato comun: nombres en snake_case). */
public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn) {

    public static TokenResponse from(TokenPair pair) {
        return new TokenResponse(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds());
    }
}

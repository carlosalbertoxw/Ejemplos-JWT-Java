package com.carlosalbertoxw.jwt.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta de login y refresh de la API (contrato comun: nombres en snake_case).
 * Solo se leen los dos tokens; el resto de campos (token_type, expires_in) se ignora.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResult(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken) {
}

package com.carlosalbertoxw.jwt.api.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de refresh y logout: el refresh token opaco. */
public record RefreshRequest(
        @JsonProperty("refresh_token")
        @NotBlank(message = "refresh_token es obligatorio") @Size(max = 200, message = "refresh_token es demasiado largo")
        String refreshToken) {
}

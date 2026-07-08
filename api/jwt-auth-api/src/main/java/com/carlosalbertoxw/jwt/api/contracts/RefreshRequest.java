package com.carlosalbertoxw.jwt.api.contracts;

import jakarta.validation.constraints.NotBlank;

/** Peticion para refrescar el par de tokens. */
public record RefreshRequest(@NotBlank String refreshToken) {
}

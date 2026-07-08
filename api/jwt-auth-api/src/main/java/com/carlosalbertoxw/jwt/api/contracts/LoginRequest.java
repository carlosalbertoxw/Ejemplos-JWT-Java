package com.carlosalbertoxw.jwt.api.contracts;

import jakarta.validation.constraints.NotBlank;

/** Credenciales de inicio de sesion. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}

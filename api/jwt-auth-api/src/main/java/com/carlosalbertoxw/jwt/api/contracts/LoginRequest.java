package com.carlosalbertoxw.jwt.api.contracts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciales de inicio de sesion (con limites de tamano para descartar payloads absurdos). */
public record LoginRequest(
        @NotBlank(message = "username es obligatorio") @Size(max = 100, message = "username es demasiado largo")
        String username,
        @NotBlank(message = "password es obligatorio") @Size(max = 200, message = "password es demasiado largo")
        String password) {
}

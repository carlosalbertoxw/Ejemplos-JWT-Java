package com.carlosalbertoxw.jwt.api.contracts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario nuevo. */
public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                message = "El usuario solo admite letras, numeros y guion bajo")
        String username,
        @NotBlank @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String password) {
}

package com.carlosalbertoxw.jwt.api.contracts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Datos para crear un producto nuevo. */
public record CreateProductRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @Positive(message = "El precio debe ser mayor que cero") double price) {
}

package com.carlosalbertoxw.jwt.api.contracts;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Datos para crear un producto nuevo. */
public record CreateProductRequest(
        @NotBlank(message = "name es obligatorio") @Size(max = 100, message = "name no puede superar los 100 caracteres")
        String name,
        @Size(max = 500, message = "description no puede superar los 500 caracteres")
        String description,
        @NotNull(message = "price es obligatorio") @DecimalMin(value = "0", message = "price debe ser mayor o igual a 0")
        BigDecimal price) {
}

package com.carlosalbertoxw.jwt.api.contracts;

/** Formato de error del contrato comun: {"error": "..."}. */
public record ErrorResponse(String error) {
}

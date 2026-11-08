package com.carlosalbertoxw.catalog.client.dto;

import java.math.BigDecimal;

/** Producto tal como lo expone la API. En el catalogo publico el precio llega vacio (null). */
public record ProductDto(int id, String name, String description, BigDecimal price) {
}

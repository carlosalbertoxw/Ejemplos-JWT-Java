package com.carlosalbertoxw.catalog.core.model;

import java.math.BigDecimal;

/**
 * Producto de ejemplo para exponer un recurso protegido y uno publico.
 * El precio usa BigDecimal: con double, sumas como 0.1 + 0.2 no son exactas.
 */
public record Product(int id, String name, String description, BigDecimal price) {
}

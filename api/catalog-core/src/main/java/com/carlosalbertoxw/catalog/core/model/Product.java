package com.carlosalbertoxw.catalog.core.model;

/**
 * Producto de ejemplo para exponer un recurso protegido y uno publico.
 */
public record Product(int id, String name, double price) {
}

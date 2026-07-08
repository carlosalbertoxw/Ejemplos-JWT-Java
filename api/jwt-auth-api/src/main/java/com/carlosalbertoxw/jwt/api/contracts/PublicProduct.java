package com.carlosalbertoxw.jwt.api.contracts;

/** Vista publica de un producto: sin precio (solo id y nombre). */
public record PublicProduct(int id, String name) {
}

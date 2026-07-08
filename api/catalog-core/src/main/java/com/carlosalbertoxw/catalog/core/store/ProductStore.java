package com.carlosalbertoxw.catalog.core.store;

import com.carlosalbertoxw.catalog.core.model.Product;
import java.util.List;

/** Almacen de productos de ejemplo. */
public interface ProductStore {

    List<Product> findAll();

    /** Agrega un producto nuevo (el id lo asigna el almacen) y lo devuelve. */
    Product add(String name, double price);
}

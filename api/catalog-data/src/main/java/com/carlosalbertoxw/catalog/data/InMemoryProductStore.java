package com.carlosalbertoxw.catalog.data;

import com.carlosalbertoxw.catalog.core.model.Product;
import com.carlosalbertoxw.catalog.core.store.ProductStore;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Catalogo de productos de ejemplo en memoria (mutable y thread-safe). */
public class InMemoryProductStore implements ProductStore {

    private final List<Product> products = new CopyOnWriteArrayList<>(List.of(
            new Product(1, "Teclado mecanico", 79.90),
            new Product(2, "Mouse inalambrico", 39.50),
            new Product(3, "Monitor 27\"", 249.00),
            new Product(4, "Auriculares", 59.99)));

    private final AtomicInteger sequence = new AtomicInteger(5);

    @Override
    public List<Product> findAll() {
        return List.copyOf(products);
    }

    @Override
    public Product add(String name, double price) {
        Product product = new Product(sequence.getAndIncrement(), name, price);
        products.add(product);
        return product;
    }
}

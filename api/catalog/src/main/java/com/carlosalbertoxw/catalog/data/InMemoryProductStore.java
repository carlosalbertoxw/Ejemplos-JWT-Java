package com.carlosalbertoxw.catalog.data;

import com.carlosalbertoxw.catalog.core.model.Product;
import com.carlosalbertoxw.catalog.core.store.ProductStore;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Catalogo de productos de ejemplo en memoria (mutable y thread-safe). */
public class InMemoryProductStore implements ProductStore {

    private final List<Product> products = new CopyOnWriteArrayList<>(List.of(
            new Product(1, "Teclado mecánico", "Switches rojos, retroiluminado", new BigDecimal("89.99")),
            new Product(2, "Mouse inalámbrico", "Sensor óptico de 16000 DPI", new BigDecimal("45.50")),
            new Product(3, "Monitor 27\"", "Panel IPS, 144 Hz", new BigDecimal("320.00")),
            new Product(4, "Dock USB-C", "HDMI doble, carga de 100 W", new BigDecimal("129.00"))));

    private final AtomicInteger sequence = new AtomicInteger(5);

    @Override
    public List<Product> findAll() {
        return List.copyOf(products);
    }

    @Override
    public Product add(String name, String description, BigDecimal price) {
        Product product = new Product(sequence.getAndIncrement(), name, description, price);
        products.add(product);
        return product;
    }
}

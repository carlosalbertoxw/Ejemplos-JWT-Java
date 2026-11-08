package com.carlosalbertoxw.jwt.api.web;

import com.carlosalbertoxw.catalog.core.model.Product;
import com.carlosalbertoxw.catalog.core.store.ProductStore;
import com.carlosalbertoxw.jwt.api.contracts.CreateProductRequest;
import com.carlosalbertoxw.jwt.api.contracts.PublicProduct;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catalogo de productos: endpoints publicos, protegidos y de administracion.
 */
@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductStore productStore;

    public ProductsController(ProductStore productStore) {
        this.productStore = productStore;
    }

    /** Publico: todos los productos pero sin el precio, sin autenticacion. */
    @GetMapping("/public")
    public List<PublicProduct> publicCatalog() {
        return productStore.findAll().stream()
                .map(p -> new PublicProduct(p.id(), p.name(), p.description()))
                .toList();
    }

    /** Protegido: requiere un access token valido (incluye el precio). */
    @GetMapping
    public List<Product> all() {
        return productStore.findAll();
    }

    /** Crea un producto nuevo. Solo el rol Admin (regla en SecurityConfig). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody CreateProductRequest request) {
        String description = request.description() == null ? "" : request.description().trim();
        return productStore.add(request.name().trim(), description, request.price());
    }
}

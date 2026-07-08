package com.carlosalbertoxw.jwt.api.web;

import com.carlosalbertoxw.jwt.api.contracts.CreateProductRequest;
import com.carlosalbertoxw.jwt.api.contracts.PublicProduct;
import com.carlosalbertoxw.catalog.core.model.Product;
import com.carlosalbertoxw.catalog.core.store.ProductStore;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
                .map(p -> new PublicProduct(p.id(), p.name()))
                .toList();
    }

    /** Protegido: requiere un access token valido. */
    @GetMapping
    public List<Product> all() {
        return productStore.findAll();
    }

    /** Crea un producto nuevo. Solo usuarios con rol ADMIN (regla en SecurityConfig). */
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest request) {
        Product created = productStore.add(request.name(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

package com.carlosalbertoxw.catalog.client;

import com.carlosalbertoxw.catalog.client.dto.ProductDto;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

/**
 * Cliente de los endpoints /api/products. Independiente de la autenticacion:
 * recibe el access token a traves de un {@link Supplier}, sin conocer el TokenStore.
 */
public class ProductsApiClient {

    private static final ParameterizedTypeReference<List<ProductDto>> LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient rest;
    private final Supplier<String> accessTokenSupplier;

    public ProductsApiClient(RestClient rest, Supplier<String> accessTokenSupplier) {
        this.rest = rest;
        this.accessTokenSupplier = accessTokenSupplier;
    }

    /** Catalogo publico, sin token. */
    public List<ProductDto> publicCatalog() {
        return rest.get()
                .uri("/api/products/public")
                .retrieve()
                .body(LIST);
    }

    /** Catalogo completo, requiere access token. */
    public List<ProductDto> all() {
        return rest.get()
                .uri("/api/products")
                .header("Authorization", "Bearer " + accessTokenSupplier.get())
                .retrieve()
                .body(LIST);
    }

    /** Crea un producto nuevo. Requiere access token con rol ADMIN. */
    public ProductDto create(String name, double price) {
        return rest.post()
                .uri("/api/products")
                .header("Authorization", "Bearer " + accessTokenSupplier.get())
                .body(Map.of("name", name, "price", price))
                .retrieve()
                .body(ProductDto.class);
    }
}

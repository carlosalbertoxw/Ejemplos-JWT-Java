package com.carlosalbertoxw.jwt.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosalbertoxw.jwt.api.contracts.AuthResponse;
import com.carlosalbertoxw.jwt.api.contracts.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void publicCatalogIsOpenButProtectedNeedsToken() {
        // El catalogo publico responde sin token, con TODOS los productos pero sin precio
        ResponseEntity<String> publicResp = rest.getForEntity("/api/products/public", String.class);
        assertThat(publicResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publicResp.getBody())
                .contains("Teclado mecanico", "Auriculares")  // incluye el primero y el ultimo
                .doesNotContain("price");                     // el precio no se expone

        // El listado completo esta protegido
        assertThat(rest.getForEntity("/api/products", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        // Login y acceso con el access token
        AuthResponse auth = rest.postForObject(
                "/api/auth/login", new LoginRequest("admin", "admin123"), AuthResponse.class);
        assertThat(auth.accessToken()).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.accessToken());
        ResponseEntity<String> protectedResp = rest.exchange(
                "/api/products", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertThat(protectedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void onlyAdminCanCreateProducts() {
        var newProduct = java.util.Map.of("name", "Webcam HD", "price", 89.90);

        // Un usuario con rol USER no puede crear productos
        HttpHeaders userHeaders = bearerHeadersFor("user", "user123");
        ResponseEntity<String> forbidden = rest.exchange(
                "/api/products", org.springframework.http.HttpMethod.POST,
                new HttpEntity<>(newProduct, userHeaders), String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Un ADMIN si puede: responde 201 y el producto aparece en el listado
        HttpHeaders adminHeaders = bearerHeadersFor("admin", "admin123");
        ResponseEntity<String> created = rest.exchange(
                "/api/products", org.springframework.http.HttpMethod.POST,
                new HttpEntity<>(newProduct, adminHeaders), String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).contains("Webcam HD");
    }

    private HttpHeaders bearerHeadersFor(String username, String password) {
        AuthResponse auth = rest.postForObject(
                "/api/auth/login", new LoginRequest(username, password), AuthResponse.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.accessToken());
        return headers;
    }
}

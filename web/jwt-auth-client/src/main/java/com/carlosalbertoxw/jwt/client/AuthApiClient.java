package com.carlosalbertoxw.jwt.client;

import com.carlosalbertoxw.jwt.client.dto.AuthResult;
import java.util.Map;
import org.springframework.web.client.RestClient;

/**
 * Cliente de los endpoints /api/auth. Guarda el par de tokens en el TokenStore.
 */
public class AuthApiClient {

    private final RestClient rest;
    private final TokenStore tokenStore;

    public AuthApiClient(RestClient rest, TokenStore tokenStore) {
        this.rest = rest;
        this.tokenStore = tokenStore;
    }

    public AuthResult login(String username, String password) {
        AuthResult result = rest.post()
                .uri("/api/auth/login")
                .body(Map.of("username", username, "password", password))
                .retrieve()
                .body(AuthResult.class);
        tokenStore.set(result);
        return result;
    }

    public void register(String username, String password) {
        rest.post()
                .uri("/api/auth/register")
                .body(Map.of("username", username, "password", password))
                .retrieve()
                .toBodilessEntity();
    }

    /** Refresca el par usando el refresh token guardado y lo rota. */
    public AuthResult refresh() {
        AuthResult result = rest.post()
                .uri("/api/auth/refresh")
                .body(Map.of("refreshToken", tokenStore.refreshToken()))
                .retrieve()
                .body(AuthResult.class);
        tokenStore.set(result);
        return result;
    }

    public void logout() {
        if (tokenStore.isLoggedIn()) {
            rest.post()
                    .uri("/api/auth/logout")
                    .header("Authorization", "Bearer " + tokenStore.accessToken())
                    .retrieve()
                    .toBodilessEntity();
        }
        tokenStore.clear();
    }
}

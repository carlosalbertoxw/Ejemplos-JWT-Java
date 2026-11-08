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

    /** Refresca el par usando el refresh token guardado y lo rota. */
    public AuthResult refresh() {
        AuthResult result = rest.post()
                .uri("/api/auth/refresh")
                .body(Map.of("refresh_token", tokenStore.refreshToken()))
                .retrieve()
                .body(AuthResult.class);
        tokenStore.set(result);
        return result;
    }

    /**
     * Revoca el refresh token en la API (cierra esta sesion) y limpia el estado local.
     *
     * <p>La peticion no lleva access token: el refresh token es la credencial, asi que
     * no hace falta renovar nada justo al salir. La sesion local se limpia en un
     * {@code finally}: aunque la API no responda, el usuario queda deslogueado.
     */
    public void logout() {
        try {
            String refreshToken = tokenStore.refreshToken();
            if (refreshToken != null) {
                rest.post()
                        .uri("/api/auth/logout")
                        .body(Map.of("refresh_token", refreshToken))
                        .retrieve()
                        .toBodilessEntity();
            }
        } finally {
            tokenStore.clear();
        }
    }
}

package com.carlosalbertoxw.jwt.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import com.carlosalbertoxw.jwt.client.dto.AuthResult;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Flujo del cliente contra una API simulada: renovacion del access token ante
 * un 401 (un solo reintento, sin bucles) y logout que limpia la sesion local
 * aunque la API no responda.
 */
class AuthClientFlowTest {

    private static final String TOKENS_NUEVOS =
            "{\"access_token\":\"nuevo\",\"refresh_token\":\"r2\",\"token_type\":\"Bearer\",\"expires_in\":900}";

    private final TokenStore store = new TokenStore();
    private MockRestServiceServer api;
    private RestClient rest;
    private AuthApiClient auth;

    @BeforeEach
    void setUp() {
        store.set(new AuthResult("viejo", "r1"));
        // Igual que WebClientConfig: el interceptor refresca con el AuthApiClient
        // que usa el mismo RestClient.
        AuthApiClient[] authHolder = new AuthApiClient[1];
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://api")
                .requestInterceptor(new TokenRefreshInterceptor(store::accessToken, () -> {
                    authHolder[0].refresh();
                    return true;
                }));
        api = MockRestServiceServer.bindTo(builder).build();
        rest = builder.build();
        auth = authHolder[0] = new AuthApiClient(rest, store);
    }

    @Test
    void un401RenuevaElTokenYReintentaConElNuevo() {
        api.expect(requestTo("http://api/api/products"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer viejo"))
                .andRespond(withUnauthorizedRequest());
        api.expect(requestTo("http://api/api/auth/refresh"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(content().json("{\"refresh_token\":\"r1\"}"))
                .andRespond(withSuccess(TOKENS_NUEVOS, MediaType.APPLICATION_JSON));
        api.expect(requestTo("http://api/api/products"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer nuevo"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        getProducts();

        api.verify();
        assertEquals("r2", store.refreshToken());
    }

    @Test
    void siElRefreshFallaSeDevuelveEl401OriginalSinMasReintentos() {
        api.expect(requestTo("http://api/api/products")).andRespond(withUnauthorizedRequest());
        api.expect(requestTo("http://api/api/auth/refresh")).andRespond(withUnauthorizedRequest());

        assertThrows(HttpClientErrorException.Unauthorized.class, this::getProducts);

        // Cualquier peticion de mas (un segundo refresh o un reintento) haria fallar verify().
        api.verify();
    }

    @Test
    void elLogoutLimpiaLaSesionAunqueLaApiNoResponda() {
        api.expect(requestTo("http://api/api/auth/logout"))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(content().json("{\"refresh_token\":\"r1\"}"))
                .andRespond(withException(new IOException("API caida")));

        assertThrows(ResourceAccessException.class, auth::logout);

        api.verify();
        assertFalse(store.isLoggedIn());
    }

    private void getProducts() {
        rest.get()
                .uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + store.accessToken())
                .retrieve()
                .toBodilessEntity();
    }
}

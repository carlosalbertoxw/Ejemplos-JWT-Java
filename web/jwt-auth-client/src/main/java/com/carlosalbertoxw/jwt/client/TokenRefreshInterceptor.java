package com.carlosalbertoxw.jwt.client;

import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Interceptor que, ante un 401 en una llamada autenticada (con header Authorization),
 * intenta refrescar el access token UNA sola vez y reintenta la peticion original.
 * Si el refresco falla, devuelve el 401 original.
 */
public class TokenRefreshInterceptor implements ClientHttpRequestInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final Supplier<String> accessTokenSupplier;
    private final TokenRefresher refresher;
    // Evita recursion/reintento durante la propia llamada de refresco.
    private final ThreadLocal<Boolean> refreshing = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public TokenRefreshInterceptor(Supplier<String> accessTokenSupplier, TokenRefresher refresher) {
        this.accessTokenSupplier = accessTokenSupplier;
        this.refresher = refresher;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        boolean unauthorized = response.getStatusCode().value() == 401;
        boolean authenticatedCall = request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
        if (!unauthorized || refreshing.get() || !authenticatedCall) {
            return response;
        }

        // Intentar refrescar el token una unica vez.
        boolean refreshed;
        refreshing.set(Boolean.TRUE);
        try {
            refreshed = refresher.refresh();
        } catch (RuntimeException ex) {
            refreshed = false;
        } finally {
            refreshing.set(Boolean.FALSE);
        }
        if (!refreshed) {
            return response;
        }

        // Reintentar la peticion original con el nuevo access token.
        response.close();
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessTokenSupplier.get());
        return execution.execute(request, body);
    }
}

package com.carlosalbertoxw.jwt.web.config;

import com.carlosalbertoxw.catalog.client.ProductsApiClient;
import com.carlosalbertoxw.jwt.client.AuthApiClient;
import com.carlosalbertoxw.jwt.client.TokenRefreshInterceptor;
import com.carlosalbertoxw.jwt.client.TokenRefresher;
import com.carlosalbertoxw.jwt.client.TokenStore;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Beans de la app web. El TokenStore y los clientes viven por sesion de usuario,
 * de modo que cada usuario mantiene su propio estado en el servidor.
 */
@Configuration
public class WebClientConfig {

    /**
     * RestClient compartido hacia la API. Lleva un interceptor que, ante un 401 en una
     * llamada autenticada, refresca el access token una vez y reintenta.
     *
     * <p>El {@link TokenStore} y el {@link AuthApiClient} son de sesion Vaadin; se resuelven
     * de forma perezosa con {@link ObjectProvider} para obtener siempre la instancia de la
     * sesion en curso y para evitar el ciclo RestClient -&gt; AuthApiClient -&gt; RestClient.
     */
    @Bean
    RestClient apiRestClient(@Value("${api.base-url}") String baseUrl,
                             ObjectProvider<TokenStore> tokenStoreProvider,
                             ObjectProvider<AuthApiClient> authApiClientProvider) {
        Supplier<String> currentAccessToken = () -> tokenStoreProvider.getObject().accessToken();
        // Si refresh() falla lanza una excepcion; el interceptor la trata como "no se pudo".
        TokenRefresher refresher = () -> {
            authApiClientProvider.getObject().refresh();
            return true;
        };
        // Timeouts explicitos: sin ellos, una API colgada dejaria la UI esperando para siempre.
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(new TokenRefreshInterceptor(currentAccessToken, refresher))
                .build();
    }

    @Bean
    @VaadinSessionScope
    TokenStore tokenStore() {
        return new TokenStore();
    }

    @Bean
    @VaadinSessionScope
    AuthApiClient authApiClient(RestClient apiRestClient, TokenStore tokenStore) {
        return new AuthApiClient(apiRestClient, tokenStore);
    }

    @Bean
    @VaadinSessionScope
    ProductsApiClient productsApiClient(RestClient apiRestClient, TokenStore tokenStore) {
        // El cliente del catalogo no conoce el TokenStore: solo recibe el access token vigente.
        return new ProductsApiClient(apiRestClient, tokenStore::accessToken);
    }
}

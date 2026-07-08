package com.carlosalbertoxw.jwt.api.config;

import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.service.AuthService;
import com.carlosalbertoxw.jwt.core.store.RefreshTokenStore;
import com.carlosalbertoxw.jwt.core.store.UserStore;
import com.carlosalbertoxw.jwt.core.tokens.TokenService;
import com.carlosalbertoxw.catalog.core.store.ProductStore;
import com.carlosalbertoxw.catalog.data.InMemoryProductStore;
import com.carlosalbertoxw.jwt.data.InMemoryRefreshTokenStore;
import com.carlosalbertoxw.jwt.data.InMemoryUserStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra como beans las piezas del nucleo de autenticacion y del catalogo
 * (servicios y stores en memoria) para inyectarlas en la capa web.
 */
@Configuration
public class CoreBeansConfig {

    @Bean
    Pbkdf2PasswordHasher passwordHasher() {
        return new Pbkdf2PasswordHasher();
    }

    @Bean
    TokenService tokenService(JwtProperties props) {
        return new TokenService(props.toSettings());
    }

    @Bean
    UserStore userStore(Pbkdf2PasswordHasher hasher) {
        return new InMemoryUserStore(hasher);
    }

    @Bean
    RefreshTokenStore refreshTokenStore() {
        return new InMemoryRefreshTokenStore();
    }

    @Bean
    ProductStore productStore() {
        return new InMemoryProductStore();
    }

    @Bean
    AuthService authService(UserStore userStore,
                            RefreshTokenStore refreshTokenStore,
                            TokenService tokenService,
                            Pbkdf2PasswordHasher hasher) {
        return new AuthService(userStore, refreshTokenStore, tokenService, hasher);
    }
}

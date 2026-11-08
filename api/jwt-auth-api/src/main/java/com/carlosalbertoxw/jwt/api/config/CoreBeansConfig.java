package com.carlosalbertoxw.jwt.api.config;

import com.carlosalbertoxw.catalog.core.store.ProductStore;
import com.carlosalbertoxw.catalog.data.InMemoryProductStore;
import com.carlosalbertoxw.jwt.api.web.AuthRateLimits;
import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.ratelimit.RateLimiter;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.service.AuthService;
import com.carlosalbertoxw.jwt.core.store.AttemptStore;
import com.carlosalbertoxw.jwt.core.store.RefreshTokenStore;
import com.carlosalbertoxw.jwt.core.store.UserStore;
import com.carlosalbertoxw.jwt.core.tokens.TokenService;
import com.carlosalbertoxw.jwt.data.InMemoryAttemptStore;
import com.carlosalbertoxw.jwt.data.InMemoryRefreshTokenStore;
import com.carlosalbertoxw.jwt.data.InMemoryUserStore;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Registra como beans las piezas del nucleo de autenticacion y del catalogo
 * (servicios y stores en memoria) para inyectarlas en la capa web.
 */
@Configuration
public class CoreBeansConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CoreBeansConfig.class);

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    Pbkdf2PasswordHasher passwordHasher() {
        return new Pbkdf2PasswordHasher();
    }

    /**
     * Ninguna clave se versiona: llega por JWT_SECRET. Si falta, con el perfil
     * "prod" la API no arranca; en desarrollo se genera una aleatoria al arrancar
     * (los tokens emitidos dejan de valer al reiniciar, aceptable en local).
     */
    @Bean
    TokenService tokenService(JwtProperties props, Environment environment, Clock clock) {
        String secret = props.secret();
        if (secret == null || secret.isBlank()) {
            if (environment.matchesProfiles("prod")) {
                throw new IllegalStateException(
                        "Con el perfil prod debes definir JWT_SECRET (>= 32 bytes).");
            }
            LOG.warn("JWT_SECRET no esta definida: se genero una clave aleatoria de desarrollo.");
            secret = TokenService.randomSecret();
        }
        JwtSettings settings = new JwtSettings(
                props.issuer(), props.audience(), secret, props.accessMinutes(), props.refreshDays());
        return new TokenService(settings, clock);
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
                            Pbkdf2PasswordHasher hasher,
                            Clock clock) {
        return new AuthService(userStore, refreshTokenStore, tokenService, hasher, clock);
    }

    /** Dos limites sobre el mismo almacen de intentos: por cuenta y por IP. */
    @Bean
    AuthRateLimits authRateLimits(RateLimitProperties props, Clock clock) {
        AttemptStore attempts = new InMemoryAttemptStore();
        return new AuthRateLimits(
                new RateLimiter(attempts, props.accountMaxAttempts(), props.window(), clock),
                new RateLimiter(attempts, props.ipMaxAttempts(), props.window(), clock));
    }
}

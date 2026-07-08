package com.carlosalbertoxw.jwt.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.model.TokenPair;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.service.AuthException;
import com.carlosalbertoxw.jwt.core.service.AuthService;
import com.carlosalbertoxw.jwt.core.tokens.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private AuthService authService;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        tokenService = new TokenService(JwtSettings.defaults());
        authService = new AuthService(
                new InMemoryUserStore(hasher),
                new InMemoryRefreshTokenStore(),
                tokenService,
                hasher);
    }

    @Test
    void loginWithValidCredentialsIssuesTokens() {
        TokenPair pair = authService.login("admin", "admin123");
        assertEquals("admin", tokenService.validateAccessToken(pair.accessToken()).getSubject());
    }

    @Test
    void loginWithWrongPasswordFails() {
        assertThrows(AuthException.class, () -> authService.login("admin", "mala"));
    }

    @Test
    void refreshRotatesTheToken() {
        TokenPair first = authService.login("user", "user123");
        TokenPair second = authService.refresh(first.refreshToken());

        assertNotEquals(first.refreshToken(), second.refreshToken());
        // El refresh anterior ya fue revocado: reusarlo debe fallar (deteccion de robo).
        assertThrows(AuthException.class, () -> authService.refresh(first.refreshToken()));
    }
}

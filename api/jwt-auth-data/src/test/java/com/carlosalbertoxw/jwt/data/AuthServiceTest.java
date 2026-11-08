package com.carlosalbertoxw.jwt.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.model.TokenPair;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.service.AuthException;
import com.carlosalbertoxw.jwt.core.service.AuthService;
import com.carlosalbertoxw.jwt.core.service.UserAlreadyExistsException;
import com.carlosalbertoxw.jwt.core.tokens.TokenService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private AuthService authService;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        // Pocas iteraciones para que las pruebas corran rapido (el hash las guarda).
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1_000);
        tokenService = new TokenService(
                new JwtSettings("iss", "aud", "secreto-de-pruebas-de-al-menos-32-bytes", 15, 7), clock);
        authService = new AuthService(
                new InMemoryUserStore(hasher), new InMemoryRefreshTokenStore(), tokenService, hasher, clock);
    }

    @Test
    void loginWithValidCredentialsIssuesTokens() {
        TokenPair pair = authService.login("admin", "Admin123!");

        var claims = tokenService.validateAccessToken(pair.accessToken());
        assertEquals("1", claims.getSubject());
        assertEquals("Admin", claims.get("role", String.class));
        assertEquals(15 * 60, pair.expiresInSeconds());
    }

    @Test
    void loginWithWrongPasswordOrUnknownUserFails() {
        assertThrows(AuthException.class, () -> authService.login("admin", "mala"));
        assertThrows(AuthException.class, () -> authService.login("no-existe", "Admin123!"));
    }

    @Test
    void refreshRotatesTheToken() {
        TokenPair first = authService.login("user", "User123!");
        TokenPair second = authService.refresh(first.refreshToken());

        assertNotEquals(first.refreshToken(), second.refreshToken());
        assertThrows(AuthException.class, () -> authService.refresh(first.refreshToken()));
    }

    @Test
    void reusingARotatedTokenRevokesEverySessionOfTheUser() {
        TokenPair first = authService.login("user", "User123!");
        TokenPair second = authService.refresh(first.refreshToken());

        // Reuso del token ya rotado (escenario de robo)...
        assertThrows(AuthException.class, () -> authService.refresh(first.refreshToken()));
        // ...revoca la familia completa: el token "bueno" tampoco sirve ya.
        assertThrows(AuthException.class, () -> authService.refresh(second.refreshToken()));
    }

    @Test
    void logoutOnlyClosesTheSessionOfTheTokenSent() {
        TokenPair phone = authService.login("user", "User123!");
        TokenPair laptop = authService.login("user", "User123!");

        authService.logout(phone.refreshToken());

        assertDoesNotThrow(() -> authService.refresh(laptop.refreshToken()));
        assertThrows(AuthException.class, () -> authService.refresh(phone.refreshToken()));
    }

    @Test
    void expiredRefreshTokenIsRejected() {
        TokenPair pair = authService.login("user", "User123!");

        clock.advance(Duration.ofDays(8));

        assertThrows(AuthException.class, () -> authService.refresh(pair.refreshToken()));
    }

    @Test
    void concurrentRefreshesWithTheSameTokenOnlyLetOneWin() throws Exception {
        TokenPair pair = authService.login("user", "User123!");
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Boolean>> results = new ArrayList<>();
        Callable<Boolean> attempt = () -> {
            start.await();
            try {
                authService.refresh(pair.refreshToken());
                return true;
            } catch (AuthException e) {
                return false;
            }
        };
        for (int i = 0; i < threads; i++) {
            results.add(pool.submit(attempt));
        }
        start.countDown();

        long winners = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                winners++;
            }
        }
        pool.shutdown();

        assertEquals(1, winners, "Solo una peticion puede canjear el mismo refresh token");
    }

    @Test
    void registeringAnExistingUsernameFails() {
        assertDoesNotThrow(() -> authService.register("nuevo", "Nuevo123!"));
        assertThrows(UserAlreadyExistsException.class, () -> authService.register("nuevo", "Otra123!"));
        assertDoesNotThrow(() -> authService.login("nuevo", "Nuevo123!"));
    }

    /** Reloj controlable para simular el paso del tiempo. */
    static final class MutableClock extends Clock {
        private volatile Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}

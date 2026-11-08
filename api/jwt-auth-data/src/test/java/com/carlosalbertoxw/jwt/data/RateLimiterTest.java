package com.carlosalbertoxw.jwt.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carlosalbertoxw.jwt.core.ratelimit.RateLimiter;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private final AuthServiceTest.MutableClock clock =
            new AuthServiceTest.MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final RateLimiter limiter =
            new RateLimiter(new InMemoryAttemptStore(), 3, Duration.ofMinutes(1), clock);

    @Test
    void blocksAfterMaxFailuresAndReportsRetryAfter() {
        for (int i = 0; i < 3; i++) {
            limiter.recordFailure("login:user:admin");
        }

        assertTrue(limiter.tooManyAttempts("login:user:admin"));
        long retryAfter = limiter.retryAfterSeconds("login:user:admin");
        assertTrue(retryAfter > 0 && retryAfter <= 60, "Retry-After fuera de rango: " + retryAfter);
    }

    @Test
    void keysAreIndependentAndClearResets() {
        for (int i = 0; i < 3; i++) {
            limiter.recordFailure("login:user:admin");
        }

        assertFalse(limiter.tooManyAttempts("login:user:user"));
        limiter.clear("login:user:admin");
        assertFalse(limiter.tooManyAttempts("login:user:admin"));
    }

    @Test
    void failuresOutsideTheWindowDoNotCount() {
        for (int i = 0; i < 3; i++) {
            limiter.recordFailure("login:ip:10.0.0.1");
        }

        clock.advance(Duration.ofSeconds(61));

        assertFalse(limiter.tooManyAttempts("login:ip:10.0.0.1"));
    }
}

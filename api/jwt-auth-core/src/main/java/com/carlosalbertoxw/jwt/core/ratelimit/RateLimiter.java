package com.carlosalbertoxw.jwt.core.ratelimit;

import com.carlosalbertoxw.jwt.core.store.AttemptStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Limite de intentos con ventana deslizante (proteccion anti fuerza bruta).
 *
 * <p>Solo se registran los intentos FALLIDOS: tras {@code maxAttempts} fallos dentro
 * de la ventana, la clave queda bloqueada hasta que el fallo mas antiguo salga de
 * ella. La clave agrupa los intentos, p. ej. {@code login:user:<usuario>} o
 * {@code login:ip:<ip>}.
 */
public class RateLimiter {

    private final AttemptStore attempts;
    private final int maxAttempts;
    private final Duration window;
    private final Clock clock;

    public RateLimiter(AttemptStore attempts, int maxAttempts, Duration window, Clock clock) {
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.window = window;
        this.clock = clock;
    }

    public boolean tooManyAttempts(String key) {
        return attempts.countSince(key, windowStart()) >= maxAttempts;
    }

    /** Segundos que faltan para poder reintentar (para el header Retry-After). */
    public long retryAfterSeconds(String key) {
        return attempts.oldestSince(key, windowStart())
                .map(oldest -> Math.max(0, Duration.between(clock.instant(), oldest.plus(window)).toSeconds()))
                .orElse(0L);
    }

    public void recordFailure(String key) {
        attempts.add(key, clock.instant());
        // Mantenimiento oportunista: descartar intentos fuera de la ventana.
        attempts.purgeBefore(windowStart());
    }

    public void clear(String key) {
        attempts.deleteKey(key);
    }

    private Instant windowStart() {
        return clock.instant().minus(window);
    }
}

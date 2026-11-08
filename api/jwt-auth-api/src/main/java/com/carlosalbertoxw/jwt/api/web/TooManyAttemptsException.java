package com.carlosalbertoxw.jwt.api.web;

/** Demasiados intentos fallidos: la API responde 429 con Retry-After. */
public class TooManyAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyAttemptsException(long retryAfterSeconds) {
        super("Demasiados intentos; espera antes de reintentar");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

package com.carlosalbertoxw.jwt.core.model;

import java.time.Instant;

/**
 * Refresh token opaco, persistido en el servidor y revocable.
 * Se rota en cada uso: al refrescar, el anterior se marca inactivo.
 */
public class RefreshToken {

    private final String token;
    private final String username;
    private final Instant expiresAt;
    private boolean active;

    public RefreshToken(String token, String username, Instant expiresAt) {
        this.token = token;
        this.username = username;
        this.expiresAt = expiresAt;
        this.active = true;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void revoke() {
        this.active = false;
    }

    public boolean isValidNow() {
        return active && Instant.now().isBefore(expiresAt);
    }
}

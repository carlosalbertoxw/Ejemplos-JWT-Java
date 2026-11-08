package com.carlosalbertoxw.jwt.core.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refresh token persistido en el servidor y revocable. El store lo indexa por el
 * hash del valor: un volcado del almacen no permite suplantar sesiones.
 * Se rota en cada uso: al refrescar, el anterior queda inactivo.
 */
public class RefreshToken {

    private final long userId;
    private final Instant expiresAt;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public RefreshToken(long userId, Instant expiresAt) {
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    /**
     * Canjea el token: lo desactiva SOLO si seguia activo y vigente, y devuelve
     * si fue esta llamada la que lo hizo. El compareAndSet es atomico: si dos
     * peticiones llegan a la vez con el mismo token, solo una obtiene true.
     * Con un "if (isActive()) revoke()" en dos pasos, ambas pasarian la
     * comprobacion y recibirian un par valido cada una.
     */
    public boolean consume(Instant now) {
        return !isExpired(now) && active.compareAndSet(true, false);
    }

    public void revoke() {
        active.set(false);
    }
}

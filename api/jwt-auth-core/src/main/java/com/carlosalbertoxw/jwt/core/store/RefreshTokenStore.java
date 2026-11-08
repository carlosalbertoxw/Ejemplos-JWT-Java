package com.carlosalbertoxw.jwt.core.store;

import com.carlosalbertoxw.jwt.core.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;

/**
 * Almacen de refresh tokens. Recibe el valor en claro, pero las implementaciones
 * deben guardar solo su hash (como haria una tabla en base de datos).
 */
public interface RefreshTokenStore {

    void save(String token, long userId, Instant expiresAt);

    Optional<RefreshToken> find(String token);

    /** Revoca todos los refresh tokens de un usuario (reaccion ante reuso). */
    void revokeAllForUser(long userId);

    /**
     * Elimina los tokens ya expirados. Los revocados aun vigentes se conservan:
     * son la evidencia que permite detectar el reuso de un token rotado.
     *
     * @return cuantos se eliminaron
     */
    int purgeExpired(Instant now);
}

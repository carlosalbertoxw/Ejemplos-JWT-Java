package com.carlosalbertoxw.jwt.core.store;

import com.carlosalbertoxw.jwt.core.model.RefreshToken;
import java.util.Optional;

/** Almacen de refresh tokens. */
public interface RefreshTokenStore {

    void save(RefreshToken token);

    Optional<RefreshToken> find(String token);

    /** Revoca todos los refresh tokens activos de un usuario (usado en logout). */
    void revokeAllForUser(String username);
}

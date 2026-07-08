package com.carlosalbertoxw.jwt.data;

import com.carlosalbertoxw.jwt.core.model.RefreshToken;
import com.carlosalbertoxw.jwt.core.store.RefreshTokenStore;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacen de refresh tokens en memoria, indexado por el valor del token.
 */
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshToken> tokens = new ConcurrentHashMap<>();

    @Override
    public void save(RefreshToken token) {
        tokens.put(token.getToken(), token);
    }

    @Override
    public Optional<RefreshToken> find(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    @Override
    public void revokeAllForUser(String username) {
        tokens.values().stream()
                .filter(t -> t.getUsername().equals(username) && t.isActive())
                .forEach(RefreshToken::revoke);
    }
}

package com.carlosalbertoxw.jwt.data;

import com.carlosalbertoxw.jwt.core.model.RefreshToken;
import com.carlosalbertoxw.jwt.core.store.RefreshTokenStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacen de refresh tokens en memoria, indexado por el hash SHA-256 del token:
 * el valor en claro nunca se guarda, igual que haria una tabla en base de datos.
 */
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshToken> tokens = new ConcurrentHashMap<>();

    @Override
    public void save(String token, long userId, Instant expiresAt) {
        tokens.put(hash(token), new RefreshToken(userId, expiresAt));
    }

    @Override
    public Optional<RefreshToken> find(String token) {
        return Optional.ofNullable(tokens.get(hash(token)));
    }

    @Override
    public void revokeAllForUser(long userId) {
        tokens.values().stream()
                .filter(t -> t.getUserId() == userId)
                .forEach(RefreshToken::revoke);
    }

    @Override
    public int purgeExpired(Instant now) {
        int before = tokens.size();
        tokens.values().removeIf(t -> t.isExpired(now));
        return before - tokens.size();
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

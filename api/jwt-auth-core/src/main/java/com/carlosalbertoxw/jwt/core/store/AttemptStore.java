package com.carlosalbertoxw.jwt.core.store;

import java.time.Instant;
import java.util.Optional;

/** Registro de intentos fallidos para el rate limiting (clave + instante de cada fallo). */
public interface AttemptStore {

    void add(String key, Instant at);

    long countSince(String key, Instant since);

    Optional<Instant> oldestSince(String key, Instant since);

    void deleteKey(String key);

    void purgeBefore(Instant before);
}

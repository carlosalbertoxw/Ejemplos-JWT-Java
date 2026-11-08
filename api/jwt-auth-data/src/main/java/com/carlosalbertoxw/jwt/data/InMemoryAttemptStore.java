package com.carlosalbertoxw.jwt.data;

import com.carlosalbertoxw.jwt.core.store.AttemptStore;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Intentos fallidos de login/refresh en memoria (para el rate limiting). */
public class InMemoryAttemptStore implements AttemptStore {

    private final Map<String, Queue<Instant>> attempts = new ConcurrentHashMap<>();

    @Override
    public void add(String key, Instant at) {
        attempts.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(at);
    }

    @Override
    public long countSince(String key, Instant since) {
        return stream(key).filter(at -> !at.isBefore(since)).count();
    }

    @Override
    public Optional<Instant> oldestSince(String key, Instant since) {
        return stream(key).filter(at -> !at.isBefore(since)).min(Instant::compareTo);
    }

    @Override
    public void deleteKey(String key) {
        attempts.remove(key);
    }

    @Override
    public void purgeBefore(Instant before) {
        attempts.values().forEach(queue -> queue.removeIf(at -> at.isBefore(before)));
        attempts.values().removeIf(Queue::isEmpty);
    }

    private java.util.stream.Stream<Instant> stream(String key) {
        Queue<Instant> queue = attempts.get(key);
        return queue == null ? java.util.stream.Stream.empty() : queue.stream();
    }
}

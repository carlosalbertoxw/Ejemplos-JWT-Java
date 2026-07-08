package com.carlosalbertoxw.jwt.data;

import com.carlosalbertoxw.jwt.core.model.AppUser;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.store.UserStore;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacen de usuarios en memoria. Siembra un admin y un usuario normal.
 */
public class InMemoryUserStore implements UserStore {

    private final Map<String, AppUser> users = new ConcurrentHashMap<>();

    /** Crea el store sembrando usuarios de demostracion (admin/admin123, user/user123). */
    public InMemoryUserStore(Pbkdf2PasswordHasher hasher) {
        seed(hasher, "admin", "admin123", Set.of("ADMIN", "USER"));
        seed(hasher, "user", "user123", Set.of("USER"));
    }

    private void seed(Pbkdf2PasswordHasher hasher, String username, String password, Set<String> roles) {
        String salt = hasher.newSalt();
        String hash = hasher.hash(password, salt);
        users.put(username, new AppUser(username, hash, salt, roles));
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void save(AppUser user) {
        users.put(user.getUsername(), user);
    }

    @Override
    public boolean exists(String username) {
        return users.containsKey(username);
    }
}

package com.carlosalbertoxw.jwt.data;

import com.carlosalbertoxw.jwt.core.model.AppUser;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.store.UserStore;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Almacen de usuarios en memoria. Siembra los usuarios de demostracion del
 * contrato comun: admin/Admin123! (rol Admin) y user/User123! (rol User).
 *
 * <p>Los nombres no distinguen mayusculas (igual que el ejemplo .NET): "Admin"
 * y "admin" son la misma cuenta, asi que nadie puede registrar una cuenta que
 * se confunda con otra, y coincide con la clave del limite de intentos.
 */
public final class InMemoryUserStore implements UserStore {

    private final Map<String, AppUser> byUsername = new ConcurrentHashMap<>();
    private final Map<Long, AppUser> byId = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public InMemoryUserStore(Pbkdf2PasswordHasher hasher) {
        create("admin", hasher.hash("Admin123!"), AppUser.ROLE_ADMIN);
        create("user", hasher.hash("User123!"), AppUser.ROLE_USER);
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(key(username)));
    }

    @Override
    public Optional<AppUser> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<AppUser> create(String username, String passwordHash, String role) {
        // computeIfAbsent es atomico por clave: solo un registro gana.
        long[] createdId = {-1};
        AppUser user = byUsername.computeIfAbsent(key(username), k -> {
            createdId[0] = sequence.getAndIncrement();
            return new AppUser(createdId[0], username, passwordHash, role);
        });
        if (user.id() != createdId[0]) {
            return Optional.empty();
        }
        byId.put(user.id(), user);
        return Optional.of(user);
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}

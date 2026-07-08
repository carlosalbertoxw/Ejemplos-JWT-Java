package com.carlosalbertoxw.jwt.core.model;

import java.util.Set;

/**
 * Usuario de la aplicacion. Inmutable: la contrasena nunca se guarda en claro,
 * se almacena el hash y la sal (ver Pbkdf2PasswordHasher).
 */
public final class AppUser {

    private final String username;
    private final String passwordHash;
    private final String salt;
    private final Set<String> roles;

    public AppUser(String username, String passwordHash, String salt, Set<String> roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.roles = Set.copyOf(roles);
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public Set<String> getRoles() {
        return roles;
    }
}

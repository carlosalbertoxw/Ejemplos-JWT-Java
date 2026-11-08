package com.carlosalbertoxw.jwt.core.model;

/**
 * Usuario de la aplicacion. Inmutable: la contrasena nunca se guarda en claro,
 * solo su hash codificado (ver Pbkdf2PasswordHasher, que incluye sal e iteraciones).
 *
 * @param id           identificador (claim "sub" del access token)
 * @param username     nombre de usuario (claim "username")
 * @param passwordHash hash PBKDF2 codificado
 * @param role         rol unico: "Admin" o "User" (claim "role")
 */
public record AppUser(long id, String username, String passwordHash, String role) {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_USER = "User";
}

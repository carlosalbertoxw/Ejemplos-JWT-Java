package com.carlosalbertoxw.jwt.core.store;

import com.carlosalbertoxw.jwt.core.model.AppUser;
import java.util.Optional;

/** Almacen de usuarios. */
public interface UserStore {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findById(long id);

    /**
     * Crea el usuario de forma atomica (el id lo asigna el almacen).
     *
     * @return el usuario creado, o vacio si el nombre ya estaba en uso
     */
    Optional<AppUser> create(String username, String passwordHash, String role);
}

package com.carlosalbertoxw.jwt.core.store;

import com.carlosalbertoxw.jwt.core.model.AppUser;
import java.util.Optional;

/** Almacen de usuarios. */
public interface UserStore {

    Optional<AppUser> findByUsername(String username);

    void save(AppUser user);

    boolean exists(String username);
}

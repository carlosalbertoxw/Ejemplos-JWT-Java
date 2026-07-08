package com.carlosalbertoxw.jwt.client;

/** Accion de refresco de tokens: devuelve {@code true} si logro renovar el par. */
@FunctionalInterface
public interface TokenRefresher {

    boolean refresh();
}

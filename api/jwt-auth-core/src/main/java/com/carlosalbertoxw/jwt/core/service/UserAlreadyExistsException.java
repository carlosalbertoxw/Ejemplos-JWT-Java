package com.carlosalbertoxw.jwt.core.service;

/** El nombre de usuario ya esta registrado (la API responde 409 Conflict). */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super("El usuario ya existe");
    }
}

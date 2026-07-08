package com.carlosalbertoxw.jwt.core.service;

/** Error de autenticacion (credenciales invalidas, refresh invalido, etc.). */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}

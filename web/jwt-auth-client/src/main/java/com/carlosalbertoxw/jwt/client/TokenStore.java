package com.carlosalbertoxw.jwt.client;

import com.carlosalbertoxw.jwt.client.dto.AuthResult;
import java.util.Base64;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Guarda el par de tokens del usuario en curso (en la app web es por sesion,
 * en el servidor: el navegador nunca ve los tokens).
 */
public class TokenStore {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private String accessToken;
    private String refreshToken;

    public void set(AuthResult result) {
        this.accessToken = result.accessToken();
        this.refreshToken = result.refreshToken();
    }

    public void clear() {
        this.accessToken = null;
        this.refreshToken = null;
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public boolean isLoggedIn() {
        return accessToken != null;
    }

    /** Nombre del usuario autenticado (claim "username" del access token). */
    public String username() {
        return claim("username");
    }

    /** Rol del usuario (claim "role"; se lee sin verificar la firma: solo para la UI). */
    public String role() {
        return claim("role");
    }

    public boolean isAdmin() {
        return "Admin".equals(role());
    }

    /** Devuelve un claim de texto del payload del access token, o null si no existe / token invalido. */
    private String claim(String name) {
        if (accessToken == null) {
            return null;
        }
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            JsonNode value = MAPPER.readTree(Base64.getUrlDecoder().decode(parts[1])).get(name);
            return value != null && value.isString() ? value.asString() : null;
        } catch (RuntimeException ignored) {
            // token con formato inesperado
            return null;
        }
    }
}

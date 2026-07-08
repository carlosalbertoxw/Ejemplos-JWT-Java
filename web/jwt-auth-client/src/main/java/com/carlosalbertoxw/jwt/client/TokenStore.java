package com.carlosalbertoxw.jwt.client;

import com.carlosalbertoxw.jwt.client.dto.AuthResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Guarda el par de tokens del usuario en curso (en la app web sera por sesion).
 */
public class TokenStore {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    /** Nombre del usuario autenticado (claim "sub" del access token). */
    public String username() {
        JsonNode sub = claim("sub");
        return sub != null ? sub.asText() : null;
    }

    /** Lee los roles del payload del access token (sin verificar firma; solo para la UI). */
    public Set<String> roles() {
        Set<String> roles = new HashSet<>();
        JsonNode rolesNode = claim("roles");
        if (rolesNode != null && rolesNode.isArray()) {
            rolesNode.forEach(node -> roles.add(node.asText()));
        }
        return roles;
    }

    public boolean hasRole(String role) {
        return roles().contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /** Devuelve un claim del payload del access token, o null si no existe / token invalido. */
    private JsonNode claim(String name) {
        if (accessToken == null) {
            return null;
        }
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            return MAPPER.readTree(payload).get(name);
        } catch (Exception ignored) {
            // token con formato inesperado
            return null;
        }
    }
}

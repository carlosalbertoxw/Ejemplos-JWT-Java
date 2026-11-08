package com.carlosalbertoxw.jwt.api.security;

/**
 * Usuario autenticado tal como lo describe el access token (principal de Spring Security).
 *
 * @param id        claim "sub"
 * @param username  claim "username"
 * @param role      claim "role"
 * @param expiresAt claim "exp" (segundos Unix)
 */
public record AuthenticatedUser(String id, String username, String role, long expiresAt) {
}

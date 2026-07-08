package com.carlosalbertoxw.jwt.core.tokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService(JwtSettings.defaults());

    @Test
    void createsAndValidatesAccessToken() {
        AppUser user = new AppUser("admin", "hash", "salt", Set.of("ADMIN", "USER"));
        String token = tokenService.createAccessToken(user);

        Claims claims = tokenService.validateAccessToken(token);

        assertEquals("admin", claims.getSubject());
        assertTrue(tokenService.rolesFrom(claims).contains("ADMIN"));
    }

    @Test
    void rejectsTamperedToken() {
        AppUser user = new AppUser("admin", "hash", "salt", Set.of("USER"));
        String token = tokenService.createAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> tokenService.validateAccessToken(tampered));
    }
}

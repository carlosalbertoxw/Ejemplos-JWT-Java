package com.carlosalbertoxw.jwt.core.tokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private static final String SECRET = "secreto-de-pruebas-de-al-menos-32-bytes";
    private static final JwtSettings SETTINGS = new JwtSettings("iss", "aud", SECRET, 15, 7);
    private static final AppUser ADMIN = new AppUser(1, "admin", "hash", AppUser.ROLE_ADMIN);

    private final TokenService tokenService = new TokenService(SETTINGS);

    @Test
    void createsAndValidatesAccessTokenWithTheCommonClaims() {
        Claims claims = tokenService.validateAccessToken(tokenService.createAccessToken(ADMIN));

        assertEquals("1", claims.getSubject());
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("Admin", claims.get("role", String.class));
        assertEquals("iss", claims.getIssuer());
    }

    @Test
    void rejectsTamperedToken() {
        String token = tokenService.createAccessToken(ADMIN);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> tokenService.validateAccessToken(tampered));
    }

    @Test
    void rejectsTokenFromAnotherIssuerOrAudience() {
        var otherIssuer = new TokenService(new JwtSettings("otro", "aud", SECRET, 15, 7));
        var otherAudience = new TokenService(new JwtSettings("iss", "otra", SECRET, 15, 7));

        assertThrows(IncorrectClaimException.class,
                () -> tokenService.validateAccessToken(otherIssuer.createAccessToken(ADMIN)));
        assertThrows(IncorrectClaimException.class,
                () -> tokenService.validateAccessToken(otherAudience.createAccessToken(ADMIN)));
    }

    @Test
    void rejectsExpiredToken() {
        Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        var past = new TokenService(SETTINGS, Clock.fixed(issuedAt, ZoneOffset.UTC));
        var later = new TokenService(SETTINGS, Clock.fixed(issuedAt.plusSeconds(16 * 60), ZoneOffset.UTC));

        String token = past.createAccessToken(ADMIN);

        assertThrows(ExpiredJwtException.class, () -> later.validateAccessToken(token));
    }

    @Test
    void rejectsSecretsShorterThan32Bytes() {
        assertThrows(IllegalArgumentException.class,
                () -> new TokenService(new JwtSettings("iss", "aud", "corta", 15, 7)));
    }
}

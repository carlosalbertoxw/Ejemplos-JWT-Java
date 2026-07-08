package com.carlosalbertoxw.jwt.core.tokens;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

/**
 * Emision y validacion de tokens JWT (access) y generacion de refresh tokens opacos.
 */
public class TokenService {

    private final JwtSettings settings;
    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public TokenService(JwtSettings settings) {
        this.settings = settings;
        this.key = Keys.hmacShaKeyFor(settings.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** Crea un access token de vida corta con el usuario y sus roles. */
    public String createAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(settings.accessMinutes() * 60);
        return Jwts.builder()
                .issuer(settings.issuer())
                .audience().add(settings.audience()).and()
                .subject(user.getUsername())
                .claim("roles", List.copyOf(user.getRoles()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /** Momento de expiracion que tendra un access token emitido ahora. */
    public Instant accessTokenExpiry() {
        return Instant.now().plusSeconds(settings.accessMinutes() * 60);
    }

    /** Genera un refresh token opaco (64 bytes aleatorios en Base64 URL-safe). */
    public String createRefreshToken() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Instant refreshTokenExpiry() {
        return Instant.now().plusSeconds(settings.refreshDays() * 24 * 60 * 60);
    }

    /**
     * Valida la firma y las condiciones temporales del access token.
     *
     * @return los claims si el token es valido
     * @throws io.jsonwebtoken.JwtException si el token es invalido o expiro
     */
    public Claims validateAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(settings.issuer())
                .requireAudience(settings.audience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> rolesFrom(Claims claims) {
        Object roles = claims.get("roles");
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }
}

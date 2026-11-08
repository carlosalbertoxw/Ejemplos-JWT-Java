package com.carlosalbertoxw.jwt.core.tokens;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import com.carlosalbertoxw.jwt.core.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

/**
 * Emision y validacion de tokens JWT (access) y generacion de refresh tokens opacos.
 *
 * <p>Claims del access token (contrato comun de los ejemplos): {@code sub} (id del
 * usuario), {@code username}, {@code role}, {@code iss}, {@code aud}, {@code iat},
 * {@code nbf}, {@code exp} y {@code jti}.
 */
public class TokenService {

    private final JwtSettings settings;
    private final SecretKey key;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public TokenService(JwtSettings settings) {
        this(settings, Clock.systemUTC());
    }

    public TokenService(JwtSettings settings, Clock clock) {
        byte[] secret = settings.secret() == null
                ? new byte[0]
                : settings.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < JwtSettings.MIN_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "La clave de firma debe tener al menos " + JwtSettings.MIN_SECRET_BYTES + " bytes");
        }
        this.settings = settings;
        this.key = Keys.hmacShaKeyFor(secret);
        this.clock = clock;
    }

    /** Crea un access token de vida corta con el usuario y su rol. */
    public String createAccessToken(AppUser user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .issuer(settings.issuer())
                .audience().add(settings.audience()).and()
                .subject(String.valueOf(user.id()))
                .claim("username", user.username())
                .claim("role", user.role())
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl())))
                .id(UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }

    public Duration accessTokenTtl() {
        return Duration.ofMinutes(settings.accessMinutes());
    }

    public Duration refreshTokenTtl() {
        return Duration.ofDays(settings.refreshDays());
    }

    /** Genera un refresh token opaco: 32 bytes aleatorios en hexadecimal (64 caracteres). */
    public String createRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return java.util.HexFormat.of().formatHex(bytes);
    }

    /**
     * Valida la firma, el emisor, la audiencia y las condiciones temporales.
     *
     * @return los claims si el token es valido
     * @throws io.jsonwebtoken.JwtException si el token es invalido o expiro
     */
    public Claims validateAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .requireIssuer(settings.issuer())
                .requireAudience(settings.audience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Codifica bytes aleatorios para pruebas o claves de desarrollo. */
    public static String randomSecret() {
        byte[] bytes = new byte[JwtSettings.MIN_SECRET_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}

package com.carlosalbertoxw.jwt.core.service;

import com.carlosalbertoxw.jwt.core.model.AppUser;
import com.carlosalbertoxw.jwt.core.model.RefreshToken;
import com.carlosalbertoxw.jwt.core.model.TokenPair;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.store.RefreshTokenStore;
import com.carlosalbertoxw.jwt.core.store.UserStore;
import com.carlosalbertoxw.jwt.core.tokens.TokenService;
import java.util.Set;

/**
 * Orquesta el flujo de autenticacion: registro, login, refresh (con rotacion) y logout.
 */
public class AuthService {

    private final UserStore userStore;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenService tokenService;
    private final Pbkdf2PasswordHasher passwordHasher;

    public AuthService(UserStore userStore,
                       RefreshTokenStore refreshTokenStore,
                       TokenService tokenService,
                       Pbkdf2PasswordHasher passwordHasher) {
        this.userStore = userStore;
        this.refreshTokenStore = refreshTokenStore;
        this.tokenService = tokenService;
        this.passwordHasher = passwordHasher;
    }

    /** Registra un usuario nuevo con los roles indicados. */
    public void register(String username, String password, Set<String> roles) {
        if (userStore.exists(username)) {
            throw new AuthException("El usuario ya existe");
        }
        String salt = passwordHasher.newSalt();
        String hash = passwordHasher.hash(password, salt);
        userStore.save(new AppUser(username, hash, salt, roles));
    }

    /** Verifica credenciales y emite un par access/refresh. */
    public TokenPair login(String username, String password) {
        AppUser user = userStore.findByUsername(username)
                .orElseThrow(() -> new AuthException("Credenciales invalidas"));
        if (!passwordHasher.verify(password, user.getSalt(), user.getPasswordHash())) {
            throw new AuthException("Credenciales invalidas");
        }
        return issuePair(user);
    }

    /**
     * Rota el refresh token: valida el actual, lo revoca y emite un par nuevo.
     * Si el token ya no es valido, se rechaza (permite detectar reuso/robo).
     */
    public TokenPair refresh(String refreshToken) {
        RefreshToken stored = refreshTokenStore.find(refreshToken)
                .orElseThrow(() -> new AuthException("Refresh token invalido"));
        // Deteccion de reuso: un token que existe pero ya fue revocado significa que
        // alguien esta reutilizando un token rotado (posible robo). Revocamos toda la
        // familia del usuario para invalidar tambien la sesion del atacante.
        if (!stored.isActive()) {
            refreshTokenStore.revokeAllForUser(stored.getUsername());
            throw new AuthException("Refresh token reutilizado: se revocaron las sesiones del usuario");
        }
        if (!stored.isValidNow()) {
            throw new AuthException("Refresh token expirado o revocado");
        }
        stored.revoke();
        refreshTokenStore.save(stored);

        AppUser user = userStore.findByUsername(stored.getUsername())
                .orElseThrow(() -> new AuthException("El usuario ya no existe"));
        return issuePair(user);
    }

    /** Revoca todos los refresh tokens del usuario. */
    public void logout(String username) {
        refreshTokenStore.revokeAllForUser(username);
    }

    private TokenPair issuePair(AppUser user) {
        String access = tokenService.createAccessToken(user);
        String refresh = tokenService.createRefreshToken();
        refreshTokenStore.save(
                new RefreshToken(refresh, user.getUsername(), tokenService.refreshTokenExpiry()));
        return new TokenPair(access, refresh, tokenService.accessTokenExpiry());
    }
}

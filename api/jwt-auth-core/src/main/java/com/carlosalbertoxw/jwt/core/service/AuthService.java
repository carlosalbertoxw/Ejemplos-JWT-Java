package com.carlosalbertoxw.jwt.core.service;

import com.carlosalbertoxw.jwt.core.model.AppUser;
import com.carlosalbertoxw.jwt.core.model.RefreshToken;
import com.carlosalbertoxw.jwt.core.model.TokenPair;
import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import com.carlosalbertoxw.jwt.core.store.RefreshTokenStore;
import com.carlosalbertoxw.jwt.core.store.UserStore;
import com.carlosalbertoxw.jwt.core.tokens.TokenService;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Orquesta el flujo de autenticacion: registro, login, refresh (con rotacion y
 * deteccion de reuso) y logout.
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private final UserStore userStore;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenService tokenService;
    private final Pbkdf2PasswordHasher passwordHasher;
    private final Clock clock;

    public AuthService(UserStore userStore,
                       RefreshTokenStore refreshTokenStore,
                       TokenService tokenService,
                       Pbkdf2PasswordHasher passwordHasher,
                       Clock clock) {
        this.userStore = userStore;
        this.refreshTokenStore = refreshTokenStore;
        this.tokenService = tokenService;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    /**
     * Registra un usuario nuevo con rol "User".
     *
     * @throws UserAlreadyExistsException si el nombre ya esta en uso
     */
    public void register(String username, String password) {
        // create() es atomico: dos registros simultaneos del mismo nombre no
        // pueden ganar ambos (un exists() seguido de save() si podria).
        userStore.create(username, passwordHasher.hash(password), AppUser.ROLE_USER)
                .orElseThrow(UserAlreadyExistsException::new);
    }

    /** Verifica credenciales y emite un par access/refresh. */
    public TokenPair login(String username, String password) {
        Optional<AppUser> user = userStore.findByUsername(username);
        // Verificar SIEMPRE contra un hash (el del usuario o el senuelo) para que
        // el tiempo de respuesta no revele si el usuario existe.
        String hash = user.map(AppUser::passwordHash).orElseGet(passwordHasher::decoyHash);
        if (!passwordHasher.verify(password, hash) || user.isEmpty()) {
            throw new AuthException("Credenciales invalidas");
        }
        return issuePair(user.get());
    }

    /**
     * Rota el refresh token: lo canjea de forma atomica y emite un par nuevo.
     * Si llega un token que ya fue usado, se asume robo y se revocan todas las
     * sesiones del usuario.
     */
    public TokenPair refresh(String refreshToken) {
        RefreshToken stored = refreshTokenStore.find(refreshToken)
                .orElseThrow(() -> new AuthException("Refresh token invalido, expirado o revocado"));

        if (!stored.consume(clock.instant())) {
            // Deteccion de reuso: el token existe pero ya no estaba activo, es decir,
            // alguien presenta un token ya rotado (o dos peticiones lo usaron a la vez).
            // Se revoca toda la familia para expulsar tambien al atacante.
            if (!stored.isActive()) {
                refreshTokenStore.revokeAllForUser(stored.getUserId());
                LOG.warning(() -> "Reuso de refresh token detectado (user_id=" + stored.getUserId()
                        + "): se revocaron todas sus sesiones");
            }
            throw new AuthException("Refresh token invalido, expirado o revocado");
        }

        AppUser user = userStore.findById(stored.getUserId())
                .orElseThrow(() -> new AuthException("Refresh token invalido, expirado o revocado"));
        return issuePair(user);
    }

    /**
     * Revoca el refresh token enviado: cierra solo esa sesion (las de otros
     * dispositivos siguen activas). Si no existe, no hace nada.
     */
    public void logout(String refreshToken) {
        refreshTokenStore.find(refreshToken).ifPresent(RefreshToken::revoke);
    }

    private TokenPair issuePair(AppUser user) {
        Instant now = clock.instant();
        // Mantenimiento oportunista: eliminar tokens ya expirados.
        refreshTokenStore.purgeExpired(now);

        String access = tokenService.createAccessToken(user);
        String refresh = tokenService.createRefreshToken();
        refreshTokenStore.save(refresh, user.id(), now.plus(tokenService.refreshTokenTtl()));
        return new TokenPair(access, refresh, tokenService.accessTokenTtl().toSeconds());
    }
}

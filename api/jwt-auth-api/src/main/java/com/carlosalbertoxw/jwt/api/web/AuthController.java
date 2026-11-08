package com.carlosalbertoxw.jwt.api.web;

import com.carlosalbertoxw.jwt.api.contracts.LoginRequest;
import com.carlosalbertoxw.jwt.api.contracts.MeResponse;
import com.carlosalbertoxw.jwt.api.contracts.MessageResponse;
import com.carlosalbertoxw.jwt.api.contracts.RefreshRequest;
import com.carlosalbertoxw.jwt.api.contracts.RegisterRequest;
import com.carlosalbertoxw.jwt.api.contracts.TokenResponse;
import com.carlosalbertoxw.jwt.api.security.AuthenticatedUser;
import com.carlosalbertoxw.jwt.core.model.TokenPair;
import com.carlosalbertoxw.jwt.core.service.AuthException;
import com.carlosalbertoxw.jwt.core.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticacion (contrato comun de los ejemplos).
 *
 * <p>Login, refresh y registro estan protegidos contra fuerza bruta: tras varios
 * intentos fallidos responden 429 con Retry-After.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthRateLimits limits;

    public AuthController(AuthService authService, AuthRateLimits limits) {
        this.authService = authService;
        this.limits = limits;
    }

    /** Valida credenciales y emite el par de tokens. */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        // Dos contadores de fallos:
        //  - por cuenta: frena la fuerza bruta contra un usuario aunque el atacante rote de IP;
        //  - por IP: frena el "password spraying" (una contrasena contra muchas cuentas).
        String accountKey = "login:user:" + request.username().toLowerCase(Locale.ROOT);
        String ipKey = "login:ip:" + http.getRemoteAddr();
        AuthRateLimits.check(limits.account(), accountKey);
        AuthRateLimits.check(limits.ip(), ipKey);

        try {
            TokenPair pair = authService.login(request.username(), request.password());
            // Solo se limpia el contador de la cuenta: si se limpiara el de la IP, un
            // atacante con una cuenta propia podria reiniciarlo a voluntad.
            limits.account().clear(accountKey);
            return TokenResponse.from(pair);
        } catch (AuthException ex) {
            limits.account().recordFailure(accountKey);
            limits.ip().recordFailure(ipKey);
            throw ex;
        }
    }

    /** Rota el refresh token y emite un par nuevo. */
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest http) {
        // Limite por IP para impedir adivinar refresh tokens. No se limpia tras un
        // exito: si no, un atacante con un token propio podria reiniciarlo.
        String ipKey = "refresh:ip:" + http.getRemoteAddr();
        AuthRateLimits.check(limits.ip(), ipKey);
        try {
            return TokenResponse.from(authService.refresh(request.refreshToken()));
        } catch (AuthException ex) {
            limits.ip().recordFailure(ipKey);
            throw ex;
        }
    }

    /**
     * Revoca el refresh token enviado y cierra solo esa sesion. Es publico: quien
     * tiene el refresh token ya puede usarlo, asi que poseerlo basta para revocarlo,
     * y el cliente no necesita un access token vigente (ni renovarlo) para salir.
     * Responde 200 aunque el token no exista, para no revelar su validez.
     */
    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return new MessageResponse("Sesión cerrada");
    }

    /** Devuelve los claims del access token recibido. */
    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return new MeResponse(user.id(), user.username(), user.role(), user.expiresAt());
    }

    /**
     * Registra un usuario con rol User (extension de este ejemplo; no forma parte
     * del contrato comun). Cuenta cada intento por IP para frenar registros masivos.
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request,
                                                    HttpServletRequest http) {
        String ipKey = "register:ip:" + http.getRemoteAddr();
        AuthRateLimits.check(limits.ip(), ipKey);
        limits.ip().recordFailure(ipKey);
        authService.register(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Usuario creado"));
    }
}

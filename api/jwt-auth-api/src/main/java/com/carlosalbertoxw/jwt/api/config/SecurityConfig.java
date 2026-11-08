package com.carlosalbertoxw.jwt.api.config;

import com.carlosalbertoxw.jwt.api.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad stateless basada en JWT:
 * endpoints de auth, catalogo publico y health abiertos; el resto requiere token.
 *
 * <p>No se configura CORS: la app web (Vaadin) llama a la API de servidor a
 * servidor, asi que ningun navegador hace peticiones cross-origin a esta API.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // Sin cookies de sesion (todo va por Authorization: Bearer), CSRF no aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // el re-dispatch a /error debe poder renderizarse (si no, un 400 se convierte en 401)
                        .requestMatchers("/error", "/health").permitAll()
                        // logout es publico: el refresh token del cuerpo es la credencial
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login", "/api/auth/refresh", "/api/auth/logout", "/api/auth/register")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/public").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // crear productos: solo el rol Admin
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("Admin")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> writeError(
                                response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Falta el access token o es invalido"))
                        .accessDeniedHandler((request, response, e) -> writeError(
                                response, HttpServletResponse.SC_FORBIDDEN, "Se requiere el rol Admin")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Errores de seguridad con el mismo formato que el resto de la API: {"error": "..."}. */
    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

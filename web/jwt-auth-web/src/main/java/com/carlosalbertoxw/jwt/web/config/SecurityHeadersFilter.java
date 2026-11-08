package com.carlosalbertoxw.jwt.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cabeceras de seguridad para todas las respuestas de la app web.
 *
 * <p>La app no usa Spring Security (la autenticacion vive en la API), asi que estas
 * cabeceras no se agregan solas: evitan que el navegador adivine tipos de contenido,
 * que otra pagina incruste la app en un iframe (clickjacking) y que se filtre la URL
 * en el Referer.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
        response.setHeader("Referrer-Policy", "same-origin");
        filterChain.doFilter(request, response);
    }
}

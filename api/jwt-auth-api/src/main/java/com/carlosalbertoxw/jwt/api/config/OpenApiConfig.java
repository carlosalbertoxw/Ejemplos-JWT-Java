package com.carlosalbertoxw.jwt.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de OpenAPI/Swagger. Define el esquema de seguridad Bearer JWT
 * para que en Swagger UI aparezca el boton "Authorize" y se puedan probar los
 * endpoints protegidos pegando el access token.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JWT Auth API",
                version = "1.0",
                description = "API de autenticacion JWT (login, refresh, logout) y catalogo de productos."),
        security = @SecurityRequirement(name = "bearer-jwt"))
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}

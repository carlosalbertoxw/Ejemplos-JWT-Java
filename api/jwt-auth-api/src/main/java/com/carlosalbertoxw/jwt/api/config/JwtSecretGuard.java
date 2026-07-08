package com.carlosalbertoxw.jwt.api.config;

import com.carlosalbertoxw.jwt.core.config.JwtSettings;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Guardia de arranque: en el perfil {@code prod} impide levantar la API con el
 * secreto de ejemplo (o vacio). Fuerza a definir {@code JWT_SECRET} en produccion.
 * En desarrollo/demo no hace nada y el secreto por defecto sigue funcionando.
 */
@Configuration
public class JwtSecretGuard {

    private static final String EXAMPLE_SECRET = JwtSettings.defaults().secret();

    public JwtSecretGuard(JwtProperties props, Environment environment) {
        boolean prod = List.of(environment.getActiveProfiles()).contains("prod");
        if (!prod) {
            return;
        }
        String secret = props.secret();
        if (secret == null || secret.isBlank() || secret.equals(EXAMPLE_SECRET)) {
            throw new IllegalStateException(
                    "En el perfil 'prod' debes definir la variable JWT_SECRET con un secreto propio "
                            + "(>= 32 bytes); no se permite arrancar con el secreto de ejemplo o vacio.");
        }
    }
}

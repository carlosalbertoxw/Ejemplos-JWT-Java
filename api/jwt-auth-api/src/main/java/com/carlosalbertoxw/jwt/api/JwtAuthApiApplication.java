package com.carlosalbertoxw.jwt.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Punto de entrada de la API REST de autenticacion. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class JwtAuthApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthApiApplication.class, args);
    }
}

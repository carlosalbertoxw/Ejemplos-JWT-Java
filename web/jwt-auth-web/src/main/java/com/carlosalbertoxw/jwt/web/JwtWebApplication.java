package com.carlosalbertoxw.jwt.web;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la aplicacion web Vaadin. */
@SpringBootApplication
public class JwtWebApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(JwtWebApplication.class, args);
    }
}

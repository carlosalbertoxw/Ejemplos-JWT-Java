package com.carlosalbertoxw.jwt.web;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion web Vaadin.
 * Desde Vaadin 25 el tema se declara explicitamente (aqui, Lumo).
 */
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
public class JwtWebApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(JwtWebApplication.class, args);
    }
}

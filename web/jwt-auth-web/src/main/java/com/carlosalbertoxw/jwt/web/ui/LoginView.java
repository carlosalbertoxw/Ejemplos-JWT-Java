package com.carlosalbertoxw.jwt.web.ui;

import com.carlosalbertoxw.jwt.client.AuthApiClient;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

/** Formulario de inicio de sesion. */
@Route("login")
public class LoginView extends VerticalLayout {

    public LoginView(AuthApiClient authApiClient) {
        setAlignItems(Alignment.CENTER);

        TextField username = new TextField("Usuario");
        PasswordField password = new PasswordField("Contrasena");

        Button login = new Button("Iniciar sesion", e -> {
            try {
                authApiClient.login(username.getValue(), password.getValue());
                // Nuevo ID de sesion al autenticarse (evita la fijacion de sesion): un
                // JSESSIONID conocido de antes del login no sirve despues. Los atributos
                // de la sesion, incluido el TokenStore recien llenado, se conservan.
                VaadinService.reinitializeSession(VaadinRequest.getCurrent());
                getUI().ifPresent(ui -> ui.navigate(ProductsView.class));
            } catch (HttpClientErrorException.TooManyRequests ex) {
                Notification.show("Demasiados intentos; espera unos minutos", 3000, Notification.Position.MIDDLE);
            } catch (HttpClientErrorException ex) {
                // 401 (credenciales) o 400 (campos vacios o demasiado largos).
                Notification.show("Credenciales invalidas", 3000, Notification.Position.MIDDLE);
            } catch (ResourceAccessException ex) {
                Notification.show("No se pudo contactar la API", 3000, Notification.Position.MIDDLE);
            } catch (RuntimeException ex) {
                Notification.show("Error inesperado al iniciar sesion", 3000, Notification.Position.MIDDLE);
            }
        });
        login.addClickShortcut(Key.ENTER);

        Paragraph hint =
                new Paragraph("Usuarios de prueba: admin / Admin123! (rol Admin) y user / User123! (rol User).");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(
                new H2("Iniciar sesion"),
                username,
                password,
                login,
                hint,
                new RouterLink("Ver catalogo publico", PublicCatalogView.class));
    }
}

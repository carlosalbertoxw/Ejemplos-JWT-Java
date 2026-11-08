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
import org.springframework.web.client.HttpClientErrorException;

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
                getUI().ifPresent(ui -> ui.navigate(ProductsView.class));
            } catch (HttpClientErrorException.TooManyRequests ex) {
                Notification.show("Demasiados intentos; espera unos minutos", 3000, Notification.Position.MIDDLE);
            } catch (RuntimeException ex) {
                Notification.show("Credenciales invalidas", 3000, Notification.Position.MIDDLE);
            }
        });
        login.addClickShortcut(Key.ENTER);

        Paragraph hint = new Paragraph("Usuarios de prueba: admin / Admin123! (rol Admin) y user / User123! (rol User).");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(new H2("Iniciar sesion"),
                username,
                password,
                login,
                hint,
                new RouterLink("Ver catalogo publico", PublicCatalogView.class));
    }
}

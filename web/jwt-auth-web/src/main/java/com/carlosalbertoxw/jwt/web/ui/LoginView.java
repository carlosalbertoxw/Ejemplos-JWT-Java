package com.carlosalbertoxw.jwt.web.ui;

import com.carlosalbertoxw.jwt.client.AuthApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Formulario de inicio de sesion. */
@Route("login")
public class LoginView extends VerticalLayout {

    public LoginView(AuthApiClient authApiClient) {
        setAlignItems(Alignment.CENTER);

        TextField username = new TextField("Usuario");
        username.setValue("admin");
        PasswordField password = new PasswordField("Contrasena");
        password.setValue("admin123");

        Button login = new Button("Iniciar sesion", e -> {
            try {
                authApiClient.login(username.getValue(), password.getValue());
                getUI().ifPresent(ui -> ui.navigate(ProductsView.class));
            } catch (Exception ex) {
                Notification.show("Credenciales invalidas", 3000, Notification.Position.MIDDLE);
            }
        });
        login.addClickShortcut(com.vaadin.flow.component.Key.ENTER);

        add(new H2("Iniciar sesion"),
                username,
                password,
                login,
                new RouterLink("Ver catalogo publico", PublicCatalogView.class));
    }
}

package com.carlosalbertoxw.jwt.web.ui;

import com.carlosalbertoxw.catalog.client.ProductsApiClient;
import com.carlosalbertoxw.catalog.client.dto.ProductDto;
import com.carlosalbertoxw.jwt.client.AuthApiClient;
import com.carlosalbertoxw.jwt.client.TokenStore;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import java.util.TreeSet;

/**
 * Catalogo completo, protegido: solo accesible con sesion iniciada.
 * Incluye un formulario para agregar productos (requiere rol ADMIN en la API).
 */
@Route("products")
public class ProductsView extends VerticalLayout implements BeforeEnterObserver {

    private final TokenStore tokenStore;
    private final ProductsApiClient productsApiClient;
    private final AuthApiClient authApiClient;

    public ProductsView(TokenStore tokenStore,
                        ProductsApiClient productsApiClient,
                        AuthApiClient authApiClient) {
        this.tokenStore = tokenStore;
        this.productsApiClient = productsApiClient;
        this.authApiClient = authApiClient;
    }

    /** Guard de ruta: si no hay token, redirige a login. */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!tokenStore.isLoggedIn()) {
            event.forwardTo(LoginView.class);
            return;
        }
        render();
    }

    private void render() {
        removeAll();

        Grid<ProductDto> grid = new Grid<>(ProductDto.class, false);
        grid.addColumn(ProductDto::id).setHeader("Id");
        grid.addColumn(ProductDto::name).setHeader("Producto");
        grid.addColumn(ProductDto::price).setHeader("Precio");

        try {
            grid.setItems(productsApiClient.all());
        } catch (Exception ex) {
            Notification.show("No se pudo cargar el catalogo (token expirado?)", 3000,
                    Notification.Position.MIDDLE);
        }

        add(buildHeader(), new H2("Catalogo completo"), grid);
        // El formulario de alta solo se muestra a usuarios con rol ADMIN
        if (tokenStore.isAdmin()) {
            add(buildAddForm());
        }
    }

    /** Barra superior: usuario autenticado, su rol y el boton de cerrar sesion (alineado a la derecha). */
    private HorizontalLayout buildHeader() {
        Span username = new Span(tokenStore.username());
        username.getStyle().set("font-weight", "bold");

        String roles = String.join(", ", new TreeSet<>(tokenStore.roles()));
        Span roleLabel = new Span(roles);
        roleLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout userBox = new VerticalLayout(username, roleLabel);
        userBox.setPadding(false);
        userBox.setSpacing(false);
        userBox.setAlignItems(FlexComponent.Alignment.END);

        Button logout = new Button("Cerrar sesion", e -> {
            authApiClient.logout();
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });

        HorizontalLayout header = new HorizontalLayout(userBox, logout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    /** Formulario para dar de alta un producto nuevo. */
    private VerticalLayout buildAddForm() {
        TextField name = new TextField("Producto");
        NumberField price = new NumberField("Precio");
        price.setMin(0.01);
        price.setStep(0.5);

        Button add = new Button("Agregar producto", e -> {
            if (name.isEmpty() || price.isEmpty() || price.getValue() <= 0) {
                Notification.show("Escribe un nombre y un precio mayor que cero", 3000,
                        Notification.Position.MIDDLE);
                return;
            }
            try {
                productsApiClient.create(name.getValue().trim(), price.getValue());
                Notification.show("Producto agregado");
                render();
            } catch (Exception ex) {
                Notification.show("No se pudo agregar el producto (sesion expirada?)", 3000,
                        Notification.Position.MIDDLE);
            }
        });

        HorizontalLayout fields = new HorizontalLayout(name, price, add);
        fields.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        fields.setPadding(false);

        VerticalLayout form = new VerticalLayout(new H3("Agregar producto"), fields);
        form.setPadding(false);
        form.setSpacing(false);
        return form;
    }
}

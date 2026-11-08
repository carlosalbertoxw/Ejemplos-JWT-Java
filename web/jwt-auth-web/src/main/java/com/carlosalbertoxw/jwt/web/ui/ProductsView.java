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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;

/**
 * Catalogo completo, protegido: solo accesible con sesion iniciada.
 * Incluye un formulario para agregar productos (requiere rol Admin en la API).
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
        grid.addColumn(ProductDto::name).setHeader("Producto");
        grid.addColumn(ProductDto::description).setHeader("Descripcion");
        grid.addColumn(p -> p.price() == null ? "" : "$" + p.price()).setHeader("Precio");

        try {
            grid.setItems(productsApiClient.all());
        } catch (RuntimeException ex) {
            Notification.show("No se pudo cargar el catalogo (sesion expirada?)", 3000,
                    Notification.Position.MIDDLE);
        }

        add(buildHeader(), new H2("Catalogo completo"), grid);
        // El formulario de alta solo se muestra al rol Admin (la API lo valida igualmente).
        if (tokenStore.isAdmin()) {
            add(buildAddForm());
        }
    }

    /** Barra superior: usuario autenticado, su rol y el boton de cerrar sesion (alineado a la derecha). */
    private HorizontalLayout buildHeader() {
        Span username = new Span(tokenStore.username());
        username.getStyle().set("font-weight", "bold");

        Span roleLabel = new Span(tokenStore.role());
        roleLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout userBox = new VerticalLayout(username, roleLabel);
        userBox.setPadding(false);
        userBox.setSpacing(false);
        userBox.setAlignItems(FlexComponent.Alignment.END);

        Button logout = new Button("Cerrar sesion", e -> {
            try {
                authApiClient.logout();
            } catch (RuntimeException ex) {
                // La sesion local ya se limpio (finally en logout); solo falto avisar a la API.
            }
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
        name.setMaxLength(100);
        TextField description = new TextField("Descripcion");
        description.setMaxLength(500);
        BigDecimalField price = new BigDecimalField("Precio");

        Button add = new Button("Agregar producto", e -> {
            if (name.isEmpty() || price.isEmpty() || price.getValue().compareTo(BigDecimal.ZERO) < 0) {
                Notification.show("Escribe un nombre y un precio mayor o igual a cero", 3000,
                        Notification.Position.MIDDLE);
                return;
            }
            try {
                productsApiClient.create(name.getValue().trim(), description.getValue().trim(), price.getValue());
                Notification.show("Producto agregado");
                render();
            } catch (RuntimeException ex) {
                Notification.show("No se pudo agregar el producto (sesion expirada?)", 3000,
                        Notification.Position.MIDDLE);
            }
        });

        HorizontalLayout fields = new HorizontalLayout(name, description, price, add);
        fields.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        fields.setPadding(false);

        VerticalLayout form = new VerticalLayout(new H3("Agregar producto"), fields);
        form.setPadding(false);
        form.setSpacing(false);
        return form;
    }
}

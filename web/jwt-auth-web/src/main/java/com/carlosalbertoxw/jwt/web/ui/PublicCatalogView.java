package com.carlosalbertoxw.jwt.web.ui;

import com.carlosalbertoxw.catalog.client.ProductsApiClient;
import com.carlosalbertoxw.catalog.client.dto.ProductDto;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Catalogo publico (sin autenticacion). Ruta raiz "/". */
@Route("")
public class PublicCatalogView extends VerticalLayout {

    public PublicCatalogView(ProductsApiClient productsApiClient) {
        Grid<ProductDto> grid = new Grid<>(ProductDto.class, false);
        grid.addColumn(ProductDto::name).setHeader("Producto");
        grid.addColumn(ProductDto::description).setHeader("Descripcion");
        // El precio no se muestra en el catalogo publico

        add(buildHeader());

        try {
            grid.setItems(productsApiClient.publicCatalog());
        } catch (Exception ex) {
            add(new Paragraph("No se pudo conectar con la API. Asegurate de que la API este corriendo (API_BASE_URL)."));
        }

        add(new H2("Catalogo publico"),
                new Paragraph("Todos los productos son visibles sin iniciar sesion, pero el precio solo se muestra al iniciar sesion."),
                grid);
    }

    /** Barra superior: enlace para iniciar sesion, alineado a la derecha. */
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout(
                new RouterLink("Iniciar sesion para ver todo", LoginView.class));
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }
}

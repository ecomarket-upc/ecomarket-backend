package pe.edu.upc.ecomarket.catalog.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.ProductQueryService;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.ProductResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.transform.ProductAssemblers;

import java.util.List;

@Tag(name = "Búsqueda", description = "Búsqueda de comercios y productos")
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductQueryService productQueryService;

    @Operation(summary = "Buscar productos por nombre, categoría y eco-etiqueta", description = "Todos los filtros son opcionales.")
    @GetMapping("/productos")
    public List<ProductResource> search(
            @Parameter(description = "Parte del nombre", example = "quinua") @RequestParam(required = false) String nombre,
            @Parameter(description = "Id de la categoría") @RequestParam(name = "categoria", required = false) Long categoryId,
            @Parameter(description = "Id de la eco-etiqueta") @RequestParam(name = "ecoEtiqueta", required = false) Long ecoLabelId) {
        return productQueryService.handle(new GetProductsQuery(null, categoryId, ecoLabelId, nombre)).stream()
                .map(ProductAssemblers::toResource)
                .toList();
    }
}

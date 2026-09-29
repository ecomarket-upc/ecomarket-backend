package pe.edu.upc.ecomarket.catalog.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.ClassifyProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsByOwnerIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.ProductCommandService;
import pe.edu.upc.ecomarket.catalog.domain.services.ProductQueryService;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.CreateProductResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.ProductClassificationResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.ProductResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.UpdateProductResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.transform.ProductAssemblers;
import pe.edu.upc.ecomarket.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

import java.util.List;

@Tag(name = "Productos", description = "Catálogo de productos de los comercios")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final IamContextFacade iamContextFacade;

    @Operation(summary = "Publicar un producto en uno de mis comercios (comerciante)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResource create(@Valid @RequestBody CreateProductResource resource) {
        return ProductAssemblers.toResource(productCommandService.handle(
                ProductAssemblers.toCommand(resource, iamContextFacade.requireCurrentRequester())));
    }

    @Operation(summary = "Listar productos de comercios aprobados", description = "Filtros opcionales por comercio, categoría y eco-etiqueta.")
    @GetMapping
    public List<ProductResource> getAll(@RequestParam(name = "comercioId", required = false) Long storeId,
                                        @RequestParam(name = "categoriaId", required = false) Long categoryId,
                                        @RequestParam(name = "ecoEtiquetaId", required = false) Long ecoLabelId) {
        return productQueryService.handle(new GetProductsQuery(storeId, categoryId, ecoLabelId, null)).stream()
                .map(ProductAssemblers::toResource)
                .toList();
    }

    @Operation(summary = "Listar los productos de todos mis comercios (comerciante)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/mis-productos")
    public List<ProductResource> getMine() {
        Long ownerId = iamContextFacade.requireCurrentRequester().userId();
        return productQueryService.handle(new GetProductsByOwnerIdQuery(ownerId)).stream()
                .map(ProductAssemblers::toResource)
                .toList();
    }

    @Operation(summary = "Consultar el detalle de un producto")
    @GetMapping("/{id}")
    public ProductResource getById(@PathVariable Long id) {
        return ProductAssemblers.toResource(productQueryService.handle(
                new GetProductByIdQuery(id, iamContextFacade.fetchCurrentRequester().orElse(null))));
    }

    @Operation(summary = "Actualizar un producto (dueño del comercio o admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PutMapping("/{id}")
    public ProductResource update(@PathVariable Long id, @Valid @RequestBody UpdateProductResource resource) {
        return ProductAssemblers.toResource(productCommandService.handle(
                ProductAssemblers.toCommand(id, resource, iamContextFacade.requireCurrentRequester())));
    }

    @Operation(summary = "Eliminar un producto (dueño del comercio o admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productCommandService.handle(new DeleteProductCommand(id, iamContextFacade.requireCurrentRequester()));
    }

    @Operation(summary = "Clasificar un producto con IA y asignarle eco-etiquetas (dueño del comercio)",
            description = "Usa Google Gemini si hay GEMINI_API_KEY; si no, un clasificador por palabras clave.")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping("/{id}/clasificar")
    public ProductClassificationResource classify(@PathVariable Long id) {
        return ProductAssemblers.toResource(productCommandService.handle(
                new ClassifyProductCommand(id, iamContextFacade.requireCurrentRequester())));
    }
}

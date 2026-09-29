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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteCategoryCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetAllCategoriesQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetCategoryByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.CategoryCommandService;
import pe.edu.upc.ecomarket.catalog.domain.services.CategoryQueryService;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.CategoryResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.SaveCategoryResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.transform.CategoryAssemblers;
import pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

import java.util.List;

@Tag(name = "Categorías", description = "Taxonomía de productos. Cualquiera puede consultarla; solo el administrador la modifica.")
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @Operation(summary = "Listar categorías")
    @GetMapping
    public List<CategoryResource> getAll() {
        return categoryQueryService.handle(new GetAllCategoriesQuery()).stream()
                .map(CategoryAssemblers::toResource)
                .toList();
    }

    @Operation(summary = "Consultar una categoría")
    @GetMapping("/{id}")
    public CategoryResource getById(@PathVariable Long id) {
        return CategoryAssemblers.toResource(categoryQueryService.handle(new GetCategoryByIdQuery(id)));
    }

    @Operation(summary = "Crear una categoría (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResource create(@Valid @RequestBody SaveCategoryResource resource) {
        return CategoryAssemblers.toResource(categoryCommandService.handle(CategoryAssemblers.toCommand(null, resource)));
    }

    @Operation(summary = "Actualizar una categoría (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoryResource update(@PathVariable Long id, @Valid @RequestBody SaveCategoryResource resource) {
        return CategoryAssemblers.toResource(categoryCommandService.handle(CategoryAssemblers.toCommand(id, resource)));
    }

    @Operation(summary = "Eliminar una categoría sin productos ni subcategorías (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryCommandService.handle(new DeleteCategoryCommand(id));
    }
}

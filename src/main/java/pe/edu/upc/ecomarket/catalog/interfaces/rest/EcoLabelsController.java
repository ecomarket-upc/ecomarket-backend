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
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteEcoLabelCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetAllEcoLabelsQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetEcoLabelByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.EcoLabelCommandService;
import pe.edu.upc.ecomarket.catalog.domain.services.EcoLabelQueryService;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.EcoLabelResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.SaveEcoLabelResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.transform.EcoLabelAssemblers;
import pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

import java.util.List;

@Tag(name = "Eco-Etiquetas", description = "Atributos ecológicos oficiales. Cualquiera puede consultarlos; solo el administrador los modifica.")
@RestController
@RequestMapping("/api/eco-etiquetas")
@RequiredArgsConstructor
public class EcoLabelsController {

    private final EcoLabelCommandService ecoLabelCommandService;
    private final EcoLabelQueryService ecoLabelQueryService;

    @Operation(summary = "Listar eco-etiquetas")
    @GetMapping
    public List<EcoLabelResource> getAll() {
        return ecoLabelQueryService.handle(new GetAllEcoLabelsQuery()).stream()
                .map(EcoLabelAssemblers::toResource)
                .toList();
    }

    @Operation(summary = "Consultar una eco-etiqueta")
    @GetMapping("/{id}")
    public EcoLabelResource getById(@PathVariable Long id) {
        return EcoLabelAssemblers.toResource(ecoLabelQueryService.handle(new GetEcoLabelByIdQuery(id)));
    }

    @Operation(summary = "Crear una eco-etiqueta (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcoLabelResource create(@Valid @RequestBody SaveEcoLabelResource resource) {
        return EcoLabelAssemblers.toResource(ecoLabelCommandService.handle(EcoLabelAssemblers.toCommand(null, resource)));
    }

    @Operation(summary = "Actualizar una eco-etiqueta (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EcoLabelResource update(@PathVariable Long id, @Valid @RequestBody SaveEcoLabelResource resource) {
        return EcoLabelAssemblers.toResource(ecoLabelCommandService.handle(EcoLabelAssemblers.toCommand(id, resource)));
    }

    @Operation(summary = "Eliminar una eco-etiqueta y quitarla de los productos (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ecoLabelCommandService.handle(new DeleteEcoLabelCommand(id));
    }
}

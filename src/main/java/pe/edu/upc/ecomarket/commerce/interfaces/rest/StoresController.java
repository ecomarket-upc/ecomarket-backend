package pe.edu.upc.ecomarket.commerce.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.DeleteStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetApprovedStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetPendingStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetStoresByOwnerIdQuery;
import pe.edu.upc.ecomarket.commerce.domain.services.StoreCommandService;
import pe.edu.upc.ecomarket.commerce.domain.services.StoreQueryService;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.SaveStoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.StoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.ValidateStoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.transform.StoreAssemblers;
import pe.edu.upc.ecomarket.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

import java.util.List;

@Tag(name = "Comercios", description = "Registro de comercios por los comerciantes y validación por el administrador")
@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class StoresController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;
    private final IamContextFacade iamContextFacade;

    @Operation(summary = "Registrar un comercio (comerciante)", description = "Queda en estado PENDING hasta que un administrador lo apruebe.")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResource create(@Valid @RequestBody SaveStoreResource resource) {
        return StoreAssemblers.toResource(storeCommandService.handle(
                StoreAssemblers.toCreateCommand(resource, iamContextFacade.requireCurrentRequester())));
    }

    @Operation(summary = "Listar comercios aprobados", description = "Filtro opcional por distrito.")
    @GetMapping
    public List<StoreResource> getApproved(@RequestParam(name = "distrito", required = false) String district) {
        return toResources(storeQueryService.handle(new GetApprovedStoresQuery(district)));
    }

    @Operation(summary = "Listar mis comercios (comerciante)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/mis-comercios")
    public List<StoreResource> getMine() {
        Long ownerId = iamContextFacade.requireCurrentRequester().userId();
        return toResources(storeQueryService.handle(new GetStoresByOwnerIdQuery(ownerId)));
    }

    @Operation(summary = "Listar comercios pendientes de validación (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pendientes")
    public List<StoreResource> getPending() {
        return toResources(storeQueryService.handle(new GetPendingStoresQuery()));
    }

    @Operation(summary = "Consultar el detalle de un comercio", description = "Los comercios no aprobados solo los ve su dueño o un administrador.")
    @GetMapping("/{id}")
    public StoreResource getById(@PathVariable Long id) {
        return StoreAssemblers.toResource(storeQueryService.handle(
                new GetStoreByIdQuery(id, iamContextFacade.fetchCurrentRequester().orElse(null))));
    }

    @Operation(summary = "Actualizar un comercio (dueño o admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PutMapping("/{id}")
    public StoreResource update(@PathVariable Long id, @Valid @RequestBody SaveStoreResource resource) {
        return StoreAssemblers.toResource(storeCommandService.handle(
                StoreAssemblers.toUpdateCommand(id, resource, iamContextFacade.requireCurrentRequester())));
    }

    @Operation(summary = "Eliminar un comercio y sus productos (dueño o admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        storeCommandService.handle(new DeleteStoreCommand(id, iamContextFacade.requireCurrentRequester()));
    }

    @Operation(summary = "Aprobar o rechazar un comercio (admin)")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/validacion")
    public StoreResource validate(@PathVariable Long id, @Valid @RequestBody ValidateStoreResource resource) {
        return StoreAssemblers.toResource(storeCommandService.handle(StoreAssemblers.toCommand(id, resource)));
    }

    private static List<StoreResource> toResources(List<Store> stores) {
        return stores.stream().map(StoreAssemblers::toResource).toList();
    }
}

package pe.edu.upc.ecomarket.iam.interfaces.rest;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.iam.domain.model.commands.DeactivateUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.ecomarket.iam.domain.services.UserCommandService;
import pe.edu.upc.ecomarket.iam.domain.services.UserQueryService;
import pe.edu.upc.ecomarket.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.UpdateUserResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.transform.UserAssemblers;
import pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

import java.util.List;

@Tag(name = "Usuarios", description = "Gestión de cuentas. El administrador ve y desactiva cuentas; cada usuario puede ver y editar la suya.")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final IamContextFacade iamContextFacade;

    @Operation(summary = "Listar usuarios (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResource> getAll() {
        return userQueryService.handle(new GetAllUsersQuery()).stream()
                .map(UserAssemblers::toResource)
                .toList();
    }

    @Operation(summary = "Consultar un usuario (admin o el propio usuario)")
    @GetMapping("/{id}")
    public UserResource getById(@PathVariable Long id) {
        return UserAssemblers.toResource(userQueryService.handle(
                new GetUserByIdQuery(id, iamContextFacade.requireCurrentRequester())));
    }

    @Operation(summary = "Actualizar nombres y apellidos (admin o el propio usuario)")
    @PutMapping("/{id}")
    public UserResource update(@PathVariable Long id, @Valid @RequestBody UpdateUserResource resource) {
        return UserAssemblers.toResource(userCommandService.handle(
                UserAssemblers.toCommand(id, resource, iamContextFacade.requireCurrentRequester())));
    }

    @Operation(summary = "Desactivar un usuario (admin, eliminación lógica)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        userCommandService.handle(new DeactivateUserCommand(id, iamContextFacade.requireCurrentRequester()));
    }
}

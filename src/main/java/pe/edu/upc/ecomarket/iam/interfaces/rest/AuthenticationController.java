package pe.edu.upc.ecomarket.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.ecomarket.iam.domain.services.UserCommandService;
import pe.edu.upc.ecomarket.iam.domain.services.UserQueryService;
import pe.edu.upc.ecomarket.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.SignInResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.SignUpResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.transform.UserAssemblers;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;
import pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@Tag(name = "Autenticación", description = "Registro, inicio de sesión y usuario actual")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final IamContextFacade iamContextFacade;

    @Operation(summary = "Registrar un consumidor o comerciante", description = "Devuelve el token para iniciar sesión de inmediato.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticatedUserResource signUp(@Valid @RequestBody SignUpResource resource) {
        return UserAssemblers.toResource(userCommandService.handle(UserAssemblers.toCommand(resource)));
    }

    @Operation(summary = "Iniciar sesión y obtener un token JWT")
    @PostMapping("/login")
    public AuthenticatedUserResource signIn(@Valid @RequestBody SignInResource resource) {
        return UserAssemblers.toResource(userCommandService.handle(UserAssemblers.toCommand(resource)));
    }

    @Operation(summary = "Consultar el usuario autenticado")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @GetMapping("/me")
    public UserResource me() {
        Requester requester = iamContextFacade.requireCurrentRequester();
        return UserAssemblers.toResource(userQueryService.handle(new GetUserByIdQuery(requester.userId(), requester)));
    }
}

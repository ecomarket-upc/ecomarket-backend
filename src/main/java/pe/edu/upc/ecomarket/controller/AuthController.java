package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.AuthRespuestaDTO;
import pe.edu.upc.ecomarket.dto.LoginDTO;
import pe.edu.upc.ecomarket.dto.RegistroDTO;
import pe.edu.upc.ecomarket.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Registro e inicio de sesión (TS01). El cierre de sesión (US03) lo hace el frontend borrando el token,
 * porque la API no guarda sesiones.
 */
@Tag(name = "Autenticación", description = "Registro e inicio de sesión con JWT (TS01). No requieren token.")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar un consumidor o comerciante (US01, US02)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthRespuestaDTO registrar(@Valid @RequestBody RegistroDTO dto) {
        return authService.registrar(dto);
    }

    @Operation(summary = "Iniciar sesión y obtener el token JWT (US03)")
    @PostMapping("/login")
    public AuthRespuestaDTO login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }
}

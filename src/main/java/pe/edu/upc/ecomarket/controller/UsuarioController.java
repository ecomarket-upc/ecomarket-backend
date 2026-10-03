package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.PerfilDTO;
import pe.edu.upc.ecomarket.dto.UsuarioDTO;
import pe.edu.upc.ecomarket.services.UsuarioService;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Perfil del usuario (US38) y gestión de cuentas por el administrador (US29, TS01).
 */
@Tag(name = "Usuarios", description = "Perfil del usuario y gestión de cuentas por el administrador (US29, US38).")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar usuarios (admin)")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @Operation(summary = "Ver mi perfil")
    @GetMapping("/perfil")
    public UsuarioDTO miPerfil() {
        return usuarioService.miPerfil();
    }

    @Operation(summary = "Editar mi perfil (US38)")
    @PutMapping("/perfil")
    public UsuarioDTO actualizarPerfil(@Valid @RequestBody PerfilDTO dto) {
        return usuarioService.actualizarPerfil(dto);
    }

    @Operation(summary = "Desactivar una cuenta (admin, US29)")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/desactivar")
    public UsuarioDTO desactivar(@PathVariable Long id) {
        return usuarioService.desactivar(id);
    }
}

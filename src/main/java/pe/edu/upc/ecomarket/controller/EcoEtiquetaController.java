package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
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
import pe.edu.upc.ecomarket.dto.EcoEtiquetaDTO;
import pe.edu.upc.ecomarket.dto.MensajeDTO;
import pe.edu.upc.ecomarket.services.EcoEtiquetaService;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Eco-Etiquetas oficiales (US28, TS05). Consultar es público; crear, editar y eliminar es del administrador.
 */
@Tag(name = "Eco-Etiquetas", description = "Eco-Etiquetas oficiales (US28, TS05). Consultar es público; escribir es del administrador.")
@RestController
@RequestMapping("/api/eco-etiquetas")
@RequiredArgsConstructor
public class EcoEtiquetaController {

    private final EcoEtiquetaService ecoEtiquetaService;

    @Operation(summary = "Listar Eco-Etiquetas activas (incluirInactivas=true para todas)")
    @GetMapping
    public List<EcoEtiquetaDTO> listar(@RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return ecoEtiquetaService.listar(incluirInactivas);
    }

    @Operation(summary = "Ver una Eco-Etiqueta")
    @GetMapping("/{id}")
    public EcoEtiquetaDTO obtener(@PathVariable Long id) {
        return ecoEtiquetaService.obtener(id);
    }

    @Operation(summary = "Crear Eco-Etiqueta (admin, US28)")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcoEtiquetaDTO crear(@Valid @RequestBody EcoEtiquetaDTO dto) {
        return ecoEtiquetaService.crear(dto);
    }

    @Operation(summary = "Editar Eco-Etiqueta (admin)")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public EcoEtiquetaDTO actualizar(@PathVariable Long id, @Valid @RequestBody EcoEtiquetaDTO dto) {
        return ecoEtiquetaService.actualizar(id, dto);
    }

    /**
     * 200 con mensaje si se desactivó porque está en productos; 204 si se borró.
     */
    @Operation(summary = "Eliminar Eco-Etiqueta: lógica (200) si está en productos, física (204) si no (TS05)")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO> eliminar(@PathVariable Long id) {
        if (ecoEtiquetaService.eliminar(id)) {
            return ResponseEntity.ok(new MensajeDTO(
                    "La Eco-Etiqueta está asignada a productos: se desactivó y se conserva en esos productos"));
        }
        return ResponseEntity.noContent().build();
    }
}

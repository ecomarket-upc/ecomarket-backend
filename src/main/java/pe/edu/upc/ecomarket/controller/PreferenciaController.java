package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.EcoEtiquetaDTO;
import pe.edu.upc.ecomarket.dto.PreferenciasDTO;
import pe.edu.upc.ecomarket.services.PreferenciaService;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Preferencias de consumo sostenible del consumidor autenticado (US04, TS11).
 */
@Tag(name = "Preferencias", description = "Preferencias de consumo sostenible (US04, TS11).")
@RestController
@RequestMapping("/api/preferencias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CONSUMIDOR')")
public class PreferenciaController {

    private final PreferenciaService preferenciaService;

    @Operation(summary = "Mis preferencias")
    @GetMapping
    public List<EcoEtiquetaDTO> listar() {
        return preferenciaService.listar();
    }

    @Operation(summary = "Guardar mis preferencias")
    @PutMapping
    public List<EcoEtiquetaDTO> guardar(@Valid @RequestBody PreferenciasDTO dto) {
        return preferenciaService.guardar(dto);
    }
}

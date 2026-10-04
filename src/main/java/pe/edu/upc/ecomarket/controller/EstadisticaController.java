package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.EstadisticasDTO;
import pe.edu.upc.ecomarket.dto.ResumenActividadDTO;
import pe.edu.upc.ecomarket.services.EstadisticaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Estadísticas del comerciante (US22, US23, US27, TS13) y resumen del consumidor (US31).
 */
@Tag(name = "Estadísticas", description = "Estadísticas del comerciante y resumen del consumidor (TS13).")
@RestController
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    /** Por ejemplo /api/comercios/1/estadisticas?periodo=mes (semana, mes o anio). */
    @Operation(summary = "Estadísticas del comercio por periodo (dueño, US22, US23, US27)")
    @PreAuthorize("hasAnyRole('COMERCIANTE', 'ADMINISTRADOR')")
    @GetMapping("/api/comercios/{id}/estadisticas")
    public EstadisticasDTO delComercio(@PathVariable Long id, @RequestParam(defaultValue = "mes") String periodo) {
        return estadisticaService.delComercio(id, periodo);
    }

    @Operation(summary = "Resumen de actividad sostenible (consumidor, US31)")
    @PreAuthorize("hasRole('CONSUMIDOR')")
    @GetMapping("/api/consumidor/resumen")
    public ResumenActividadDTO resumen() {
        return estadisticaService.resumenConsumidor();
    }
}

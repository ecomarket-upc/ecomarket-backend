package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.RecomendacionComerciosDTO;
import pe.edu.upc.ecomarket.dto.RecomendacionProductosDTO;
import pe.edu.upc.ecomarket.services.RecomendacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Recomendaciones para el consumidor autenticado (US17, US18, TS12).
 */
@Tag(name = "Recomendaciones", description = "Productos y comercios recomendados (US17, US18, TS12).")
@RestController
@RequestMapping("/api/recomendaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CONSUMIDOR')")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @Operation(summary = "Productos recomendados (US17)")
    @GetMapping("/productos")
    public RecomendacionProductosDTO productos() {
        return recomendacionService.productos();
    }

    @Operation(summary = "Comercios cercanos recomendados (US18)")
    @GetMapping("/comercios")
    public RecomendacionComerciosDTO comercios(@RequestParam double lat, @RequestParam double lng,
                                               @RequestParam(defaultValue = "5") double radio) {
        return recomendacionService.comercios(lat, lng, radio);
    }
}

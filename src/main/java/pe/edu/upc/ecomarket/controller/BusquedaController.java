package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.services.ComercioService;

import java.util.List;

/**
 * Búsquedas públicas de comercios y productos (TS03, TS04).
 */
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final ComercioService comercioService;

    /** US05, US07: comercios aprobados en un radio de hasta 50 km, ordenados por distancia. */
    @GetMapping("/cercanos")
    public List<ComercioCercanoDTO> cercanos(@RequestParam double lat, @RequestParam double lng,
                                             @RequestParam(defaultValue = "5") double radio) {
        return comercioService.cercanos(lat, lng, radio);
    }
}

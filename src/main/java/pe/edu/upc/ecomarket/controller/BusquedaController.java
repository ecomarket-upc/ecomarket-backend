package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ComercioRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.models.Busqueda;
import pe.edu.upc.ecomarket.services.ActividadService;
import pe.edu.upc.ecomarket.services.ComercioService;
import pe.edu.upc.ecomarket.services.ProductoService;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Búsquedas públicas de comercios y productos (TS03, TS04).
 */
@Tag(name = "Búsqueda", description = "Búsqueda por cercanía y por texto (TS03, TS04). Pública.")
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final ComercioService comercioService;
    private final ProductoService productoService;
    private final ActividadService actividadService;

    /** US05, US07: comercios aprobados en un radio de hasta 50 km, ordenados por distancia. */
    @Operation(summary = "Comercios aprobados cercanos, ordenados por distancia (US05, TS03)")
    @GetMapping("/cercanos")
    public List<ComercioCercanoDTO> cercanos(@RequestParam double lat, @RequestParam double lng,
                                             @RequestParam(defaultValue = "5") double radio) {
        return comercioService.cercanos(lat, lng, radio);
    }

    /**
     * US06, US07, US15, TS04: por ejemplo /api/busqueda/productos?nombre=quinua&categoria=Alimentos&ecoEtiqueta=granel.
     * Solo devuelve productos de comercios aprobados; lista vacía si no hay coincidencias.
     * Primero los destacados (premium, US25) y luego los afines a las preferencias del consumidor (US04).
     */
    @Operation(summary = "Buscar productos por nombre, categoría y Eco-Etiqueta (US06, US07, US15, TS04)")
    @GetMapping("/productos")
    public List<ProductoRespuestaDTO> productos(@RequestParam(required = false) String nombre,
                                                @RequestParam(required = false) String categoria,
                                                @RequestParam(required = false) String ecoEtiqueta) {
        actividadService.registrarBusqueda(nombre, Busqueda.TIPO_PRODUCTO);
        return productoService.buscarOrdenado(nombre, categoria, ecoEtiqueta);
    }

    /** US06, escenario 3: comercios aprobados cuyo nombre contiene el texto. */
    @Operation(summary = "Buscar comercios aprobados por nombre (US06)")
    @GetMapping("/comercios")
    public List<ComercioRespuestaDTO> comercios(@RequestParam String texto) {
        actividadService.registrarBusqueda(texto, Busqueda.TIPO_COMERCIO);
        return comercioService.buscarPorTexto(texto);
    }
}

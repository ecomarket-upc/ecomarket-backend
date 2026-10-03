package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ComercioRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.services.ComercioService;
import pe.edu.upc.ecomarket.services.ProductoService;

import java.util.List;

/**
 * Búsquedas públicas de comercios y productos (TS03, TS04).
 */
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final ComercioService comercioService;
    private final ProductoService productoService;

    /** US05, US07: comercios aprobados en un radio de hasta 50 km, ordenados por distancia. */
    @GetMapping("/cercanos")
    public List<ComercioCercanoDTO> cercanos(@RequestParam double lat, @RequestParam double lng,
                                             @RequestParam(defaultValue = "5") double radio) {
        return comercioService.cercanos(lat, lng, radio);
    }

    /**
     * US06, US07, US15, TS04: por ejemplo /api/busqueda/productos?nombre=quinua&categoria=Alimentos&ecoEtiqueta=granel.
     * Solo devuelve productos de comercios aprobados; lista vacía si no hay coincidencias.
     */
    @GetMapping("/productos")
    public List<ProductoRespuestaDTO> productos(@RequestParam(required = false) String nombre,
                                                @RequestParam(required = false) String categoria,
                                                @RequestParam(required = false) String ecoEtiqueta) {
        return productoService.buscar(nombre, categoria, ecoEtiqueta).stream().map(productoService::aDTO).toList();
    }

    /** US06, escenario 3: comercios aprobados cuyo nombre contiene el texto. */
    @GetMapping("/comercios")
    public List<ComercioRespuestaDTO> comercios(@RequestParam String texto) {
        return comercioService.buscarPorTexto(texto);
    }
}

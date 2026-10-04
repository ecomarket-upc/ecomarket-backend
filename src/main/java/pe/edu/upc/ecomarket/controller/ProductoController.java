package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
import pe.edu.upc.ecomarket.dto.ClasificacionDTO;
import pe.edu.upc.ecomarket.dto.ProductoComparadoDTO;
import pe.edu.upc.ecomarket.dto.ProductoDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.services.ActividadService;
import pe.edu.upc.ecomarket.services.ProductoService;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Productos (TS04): el comerciante publica y administra; el detalle y la comparación son públicos.
 */
@Tag(name = "Productos", description = "Productos, clasificación con IA y comparación (TS04, TS06).")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ActividadService actividadService;

    @Operation(summary = "Publicar producto y clasificarlo con IA (comerciante, US13, US20)")
    @PreAuthorize("hasRole('COMERCIANTE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoRespuestaDTO crear(@Valid @RequestBody ProductoDTO dto) {
        return productoService.crear(dto);
    }

    @Operation(summary = "Mis productos (comerciante)")
    @PreAuthorize("hasRole('COMERCIANTE')")
    @GetMapping("/mis-productos")
    public List<ProductoRespuestaDTO> misProductos() {
        return productoService.misProductos();
    }

    /** US12: por ejemplo /api/productos/comparar?ids=1,2&lat=-12.10&lng=-77.04 */
    @Operation(summary = "Comparar de 2 a 4 productos (US12)")
    @GetMapping("/comparar")
    public List<ProductoComparadoDTO> comparar(@RequestParam List<Long> ids,
                                               @RequestParam(required = false) Double lat,
                                               @RequestParam(required = false) Double lng) {
        return productoService.comparar(ids, lat, lng);
    }

    /** US11; TS13: registra la visita al producto (y la búsqueda de origen con ?busqueda=termino). */
    @Operation(summary = "Detalle del producto y registro de la visita (US11, TS13)")
    @GetMapping("/{id}")
    public ProductoRespuestaDTO obtener(@PathVariable Long id, @RequestParam(required = false) String busqueda) {
        Producto producto = productoService.obtenerVisible(id);
        actividadService.registrarVisita(producto.getComercio(), producto, busqueda);
        return productoService.aDTO(producto);
    }

    @Operation(summary = "Editar producto (dueño o admin, US16, US20)")
    @PutMapping("/{id}")
    public ProductoRespuestaDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDTO dto) {
        return productoService.actualizar(id, dto);
    }

    /** TS06: vuelve a clasificar con Gemini (o palabras clave) e indica el motor usado. */
    @Operation(summary = "Volver a clasificar con Gemini o palabras clave (dueño, TS06)")
    @PostMapping("/{id}/clasificar")
    public ClasificacionDTO clasificar(@PathVariable Long id) {
        return productoService.clasificar(id);
    }

    @Operation(summary = "Eliminar producto (dueño o admin, US29)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}

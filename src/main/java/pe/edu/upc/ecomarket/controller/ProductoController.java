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
import pe.edu.upc.ecomarket.dto.ProductoComparadoDTO;
import pe.edu.upc.ecomarket.dto.ProductoDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.services.ProductoService;

import java.util.List;

/**
 * Productos (TS04): el comerciante publica y administra; el detalle y la comparación son públicos.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PreAuthorize("hasRole('COMERCIANTE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoRespuestaDTO crear(@Valid @RequestBody ProductoDTO dto) {
        return productoService.crear(dto);
    }

    @PreAuthorize("hasRole('COMERCIANTE')")
    @GetMapping("/mis-productos")
    public List<ProductoRespuestaDTO> misProductos() {
        return productoService.misProductos();
    }

    /** US12: por ejemplo /api/productos/comparar?ids=1,2&lat=-12.10&lng=-77.04 */
    @GetMapping("/comparar")
    public List<ProductoComparadoDTO> comparar(@RequestParam List<Long> ids,
                                               @RequestParam(required = false) Double lat,
                                               @RequestParam(required = false) Double lng) {
        return productoService.comparar(ids, lat, lng);
    }

    @GetMapping("/{id}")
    public ProductoRespuestaDTO obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PutMapping("/{id}")
    public ProductoRespuestaDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDTO dto) {
        return productoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}

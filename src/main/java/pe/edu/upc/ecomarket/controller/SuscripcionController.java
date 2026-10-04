package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.PromocionDTO;
import pe.edu.upc.ecomarket.dto.PromocionRespuestaDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionRespuestaDTO;
import pe.edu.upc.ecomarket.services.SuscripcionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Plan premium (pago simulado) y promociones (US24, US26, TS14).
 */
@Tag(name = "Premium y promociones", description = "Plan premium simulado y promociones (TS14).")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMERCIANTE')")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @Operation(summary = "Contratar plan premium (pago simulado, US24)")
    @PostMapping("/api/suscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    public SuscripcionRespuestaDTO contratar(@Valid @RequestBody SuscripcionDTO dto) {
        return suscripcionService.contratar(dto);
    }

    @Operation(summary = "Ver plan del comercio (US24)")
    @GetMapping("/api/suscripciones")
    public SuscripcionRespuestaDTO consultar(@RequestParam Long comercioId) {
        return suscripcionService.consultar(comercioId);
    }

    @Operation(summary = "Publicar promoción (solo premium, US26)")
    @PostMapping("/api/promociones")
    @ResponseStatus(HttpStatus.CREATED)
    public PromocionRespuestaDTO crearPromocion(@Valid @RequestBody PromocionDTO dto) {
        return suscripcionService.crearPromocion(dto);
    }
}

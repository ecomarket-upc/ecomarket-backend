package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.PromocionDTO;
import pe.edu.upc.ecomarket.dto.PromocionRespuestaDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionRespuestaDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Promocion;
import pe.edu.upc.ecomarket.models.Suscripcion;
import pe.edu.upc.ecomarket.repository.PromocionRepository;
import pe.edu.upc.ecomarket.repository.SuscripcionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Plan premium (pago simulado) y promociones de los comercios (US24, US26, TS14).
 */
@Service
@RequiredArgsConstructor
public class SuscripcionService {

    /** Precio mensual simulado del plan premium. */
    public static final BigDecimal PRECIO_MENSUAL = new BigDecimal("29.90");

    private final SuscripcionRepository suscripcionRepository;
    private final PromocionRepository promocionRepository;
    private final ComercioService comercioService;

    /**
     * US24: activa el plan desde hoy (o lo extiende si ya está activo). No hay pasarela de pago real.
     */
    @Transactional
    public SuscripcionRespuestaDTO contratar(SuscripcionDTO dto) {
        Comercio comercio = comercioService.buscarPropio(dto.getComercioId(), "Solo puedes contratar planes para tus propios comercios");
        if (comercio.getEstado() != EstadoComercio.APPROVED) {
            throw new ReglaNegocioException("El comercio debe estar aprobado para contratar el plan premium");
        }
        BigDecimal monto = PRECIO_MENSUAL.multiply(BigDecimal.valueOf(dto.getMeses()));
        Suscripcion actual = suscripcionActiva(comercio.getId());
        if (actual != null) {
            // Renovación: se suman los meses a la fecha de fin del plan vigente.
            actual.setFechaFin(actual.getFechaFin().plusMonths(dto.getMeses()));
            actual.setMonto(actual.getMonto().add(monto));
            return aDTO(suscripcionRepository.save(actual));
        }
        LocalDate hoy = LocalDate.now();
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setComercio(comercio);
        suscripcion.setMonto(monto);
        suscripcion.setFechaInicio(hoy);
        suscripcion.setFechaFin(hoy.plusMonths(dto.getMeses()).minusDays(1));
        return aDTO(suscripcionRepository.save(suscripcion));
    }

    /** US24 escenario 2: si el plan venció, lo informa y ofrece renovarlo. */
    public SuscripcionRespuestaDTO consultar(Long comercioId) {
        comercioService.buscarPropio(comercioId, "Solo puedes consultar los planes de tus propios comercios");
        List<Suscripcion> suscripciones = suscripcionRepository.findByComercioIdOrderByFechaFinDesc(comercioId);
        if (suscripciones.isEmpty()) {
            SuscripcionRespuestaDTO sinPlan = new SuscripcionRespuestaDTO();
            sinPlan.setComercioId(comercioId);
            sinPlan.setPlan("GRATUITO");
            sinPlan.setMensaje("El comercio usa el plan gratuito. Puedes contratar el plan premium");
            return sinPlan;
        }
        SuscripcionRespuestaDTO dto = aDTO(suscripciones.getFirst());
        if (!dto.isActiva()) {
            dto.setMensaje("Tu plan premium venció y sus funciones se desactivaron. Puedes renovarlo");
        }
        return dto;
    }

    /** US26 y TS14: solo con plan premium activo (403) y con fechas válidas (400). */
    @Transactional
    public PromocionRespuestaDTO crearPromocion(PromocionDTO dto) {
        Comercio comercio = comercioService.buscarPropio(dto.getComercioId(), "Solo puedes publicar promociones en tus propios comercios");
        if (!comercioService.esPremium(comercio.getId())) {
            throw new AccesoDenegadoException("Necesitas un plan premium activo para publicar promociones");
        }
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin (fechaFin) no puede ser anterior a la fecha de inicio");
        }
        Promocion promocion = new Promocion();
        promocion.setComercio(comercio);
        promocion.setTitulo(dto.getTitulo().trim());
        promocion.setDescripcion(dto.getDescripcion());
        promocion.setFechaInicio(dto.getFechaInicio());
        promocion.setFechaFin(dto.getFechaFin());
        Promocion guardada = promocionRepository.save(promocion);
        return new PromocionRespuestaDTO(guardada.getId(), comercio.getId(), guardada.getTitulo(),
                guardada.getDescripcion(), guardada.getFechaInicio(), guardada.getFechaFin());
    }

    private Suscripcion suscripcionActiva(Long comercioId) {
        return suscripcionRepository.findByComercioIdOrderByFechaFinDesc(comercioId).stream()
                .filter(Suscripcion::estaActiva).findFirst().orElse(null);
    }

    private SuscripcionRespuestaDTO aDTO(Suscripcion suscripcion) {
        SuscripcionRespuestaDTO dto = new SuscripcionRespuestaDTO();
        dto.setId(suscripcion.getId());
        dto.setComercioId(suscripcion.getComercio().getId());
        dto.setPlan(suscripcion.getPlan());
        dto.setMonto(suscripcion.getMonto());
        dto.setFechaInicio(suscripcion.getFechaInicio());
        dto.setFechaFin(suscripcion.getFechaFin());
        dto.setActiva(suscripcion.estaActiva());
        return dto;
    }
}

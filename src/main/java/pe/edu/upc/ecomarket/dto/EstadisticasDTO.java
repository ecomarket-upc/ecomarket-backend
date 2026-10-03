package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Estadísticas del comercio en un periodo (US22, US23, US27, TS13).
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstadisticasDTO {

    private Long comercioId;
    private String periodo;
    private LocalDate desde;
    private LocalDate hasta;
    private long visitasComercio;
    private List<ConteoDTO> visitasProductos;
    private List<ConteoDTO> terminosBusqueda;
    /** "Aún no hay visitas en este periodo" (US22, escenario 2). */
    private String mensajeVisitas;
    /** "Aún no hay búsquedas registradas" (US23, escenario 2). */
    private String mensajeBusquedas;
    private boolean premium;
    /** Solo con plan premium activo (US27). */
    private EstadisticasAvanzadasDTO avanzadas;
    /** Invitación a contratar el plan cuando no es premium (US27, escenario 2). */
    private String mensajePremium;
}

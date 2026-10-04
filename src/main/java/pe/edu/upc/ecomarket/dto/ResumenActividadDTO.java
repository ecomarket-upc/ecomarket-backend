package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resumen de actividad sostenible del consumidor (US31).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumenActividadDTO {

    private List<ConteoDTO> productosConsultadosPorEcoEtiqueta;
    private long comerciosFavoritos;
    /** Invitación a explorar cuando aún no hay actividad (US31, escenario 2). */
    private String mensaje;
}

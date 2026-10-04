package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Eco-Etiquetas que el consumidor elige como preferencias (US04, TS11). Una lista vacía las borra.
 */
@Data
public class PreferenciasDTO {

    @NotNull(message = "La lista de Eco-Etiquetas es obligatoria")
    @Schema(example = "[1, 5]")
    private Set<Long> ecoEtiquetaIds;
}

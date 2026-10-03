package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * Eco-Etiquetas que el consumidor elige como preferencias (US04, TS11). Una lista vacía las borra.
 */
@Data
public class PreferenciasDTO {

    @NotNull(message = "La lista de Eco-Etiquetas es obligatoria")
    private Set<Long> ecoEtiquetaIds;
}

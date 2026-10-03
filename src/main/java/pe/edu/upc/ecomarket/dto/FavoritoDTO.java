package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Comercio que el consumidor quiere guardar como favorito (US09, TS11).
 */
@Data
public class FavoritoDTO {

    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;
}

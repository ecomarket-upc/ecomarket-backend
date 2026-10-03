package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Comercio que el consumidor quiere guardar como favorito (US09, TS11).
 */
@Data
public class FavoritoDTO {

    @Schema(example = "1")
    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;
}

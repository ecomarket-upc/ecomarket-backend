package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Decisión del administrador sobre un comercio: APPROVED o REJECTED (US30, TS02).
 */
@Data
public class ValidacionComercioDTO {

    @Schema(example = "APPROVED")
    @NotNull(message = "El estado es obligatorio (APPROVED o REJECTED)")
    private EstadoComercio estado;

    /** Obligatorio cuando se rechaza. */
    @Schema(example = "Datos verificados")
    @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
    private String motivo;
}

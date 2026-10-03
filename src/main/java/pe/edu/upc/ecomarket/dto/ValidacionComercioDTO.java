package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.edu.upc.ecomarket.models.EstadoComercio;

/**
 * Decisión del administrador sobre un comercio: APPROVED o REJECTED (US30, TS02).
 */
@Data
public class ValidacionComercioDTO {

    @NotNull(message = "El estado es obligatorio (APPROVED o REJECTED)")
    private EstadoComercio estado;

    /** Obligatorio cuando se rechaza. */
    @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
    private String motivo;
}

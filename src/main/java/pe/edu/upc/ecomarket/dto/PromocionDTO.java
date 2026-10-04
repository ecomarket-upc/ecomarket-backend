package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Promoción de un comercio premium (US26, TS14).
 */
@Data
public class PromocionDTO {

    @Schema(example = "1")
    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;

    @Schema(example = "2x1 en granos")
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar 100 caracteres")
    private String titulo;

    @Schema(example = "Solo esta semana")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @Schema(example = "2026-10-03")
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @Schema(example = "2026-10-10")
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;
}

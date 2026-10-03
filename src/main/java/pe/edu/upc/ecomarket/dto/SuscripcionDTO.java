package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Contratación simulada del plan premium para un comercio (US24, TS14).
 */
@Data
public class SuscripcionDTO {

    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;

    @NotNull(message = "La cantidad de meses es obligatoria")
    @Min(value = 1, message = "El plan se contrata por al menos 1 mes")
    @Max(value = 12, message = "El plan se contrata por máximo 12 meses")
    private Integer meses;
}

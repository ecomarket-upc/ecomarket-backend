package pe.edu.upc.ecomarket.commerce.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;

public record ValidateStoreResource(
        @Schema(description = "APPROVED o REJECTED", example = "APPROVED")
        @NotNull(message = "La decisión es obligatoria")
        StoreStatus status,

        @Schema(description = "Obligatorio si se rechaza", example = "Datos verificados")
        @Size(max = 300, message = "El comentario no puede superar 300 caracteres")
        String comment
) {
}

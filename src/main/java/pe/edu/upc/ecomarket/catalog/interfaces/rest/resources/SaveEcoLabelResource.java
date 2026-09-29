package pe.edu.upc.ecomarket.catalog.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SaveEcoLabelResource(
        @Schema(example = "Venta a granel")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String name,

        @Schema(example = "Se vende por peso, sin empaque individual")
        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description,

        @Schema(example = "El cliente lleva su envase o compra la cantidad exacta que necesita")
        @Size(max = 500, message = "El criterio no puede superar 500 caracteres")
        String criteria,

        @Schema(description = "Palabras que usa el clasificador sin IA", example = "[\"granel\", \"por kilo\"]")
        Set<@Size(max = 60, message = "Cada palabra clave puede tener hasta 60 caracteres") String> keywords
) {
}

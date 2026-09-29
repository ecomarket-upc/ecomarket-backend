package pe.edu.upc.ecomarket.catalog.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveCategoryResource(
        @Schema(example = "Alimentos a granel")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String name,

        @Schema(example = "Granos, menestras y frutos secos vendidos por peso")
        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description,

        @Schema(description = "Id de la categoría padre; omitir para una categoría principal")
        Long parentId
) {
}

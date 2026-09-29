package pe.edu.upc.ecomarket.catalog.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Same fields as {@link CreateProductResource} except the store, which cannot change.
 */
public record UpdateProductResource(
        @Schema(example = "1")
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        @Schema(example = "Quinua blanca a granel")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String description,

        @Schema(example = "11.90")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal price,

        @Schema(example = "35")
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 300, message = "La URL de la imagen no puede superar 300 caracteres")
        String imageUrl,

        Set<Long> ecoLabelIds
) {
}

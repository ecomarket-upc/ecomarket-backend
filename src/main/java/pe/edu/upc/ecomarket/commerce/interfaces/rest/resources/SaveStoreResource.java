package pe.edu.upc.ecomarket.commerce.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body to create or update a store. If latitude and longitude are omitted, the address is geocoded.
 */
public record SaveStoreResource(
        @Schema(example = "Granel Verde")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Schema(example = "Tienda a granel de productos orgánicos y sin empaques plásticos")
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        @Schema(example = "987654321")
        @Pattern(regexp = "^[0-9+ ]{6,20}$", message = "El teléfono solo puede tener números, espacios y +")
        String phone,

        @Schema(example = "contacto@granelverde.pe")
        @Email(message = "El correo de contacto no tiene un formato válido")
        @Size(max = 120, message = "El correo de contacto no puede superar 120 caracteres")
        String contactEmail,

        @Schema(example = "Lun-Sáb 9:00-19:00")
        @Size(max = 120, message = "El horario no puede superar 120 caracteres")
        String openingHours,

        @Schema(example = "Av. Primavera 2390")
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 160, message = "La dirección no puede superar 160 caracteres")
        String addressLine,

        @Schema(example = "Santiago de Surco")
        @NotBlank(message = "El distrito es obligatorio")
        @Size(max = 80, message = "El distrito no puede superar 80 caracteres")
        String district,

        @Schema(example = "Lima")
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
        String city,

        @Schema(description = "Opcional. Si se omite, se calcula a partir de la dirección", example = "-12.1040")
        @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
        Double latitude,

        @Schema(description = "Opcional. Si se omite, se calcula a partir de la dirección", example = "-76.9630")
        @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
        Double longitude
) {
}

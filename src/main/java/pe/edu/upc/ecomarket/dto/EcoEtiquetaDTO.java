package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoEtiquetaDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(example = "Comercio justo")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
    private String nombre;

    @Schema(example = "Pago justo a productores")
    @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
    private String descripcion;

    @Schema(example = "Compra directa al productor")
    @Size(max = 500, message = "El criterio no puede superar 500 caracteres")
    private String criterio;

    @Schema(example = "comercio justo, compra directa")
    @Size(max = 500, message = "Las palabras clave no pueden superar 500 caracteres")
    private String palabrasClave;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean activa;
}

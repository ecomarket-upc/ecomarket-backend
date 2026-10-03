package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoEtiquetaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
    private String nombre;

    @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
    private String descripcion;

    @Size(max = 500, message = "El criterio no puede superar 500 caracteres")
    private String criterio;

    @Size(max = 500, message = "Las palabras clave no pueden superar 500 caracteres")
    private String palabrasClave;

    private Boolean activa;
}

package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos para crear una cuenta de consumidor o de comerciante (US01, US02).
 */
@Data
public class RegistroDTO {

    @Schema(example = "Lucía")
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80, message = "Los nombres no pueden superar 80 caracteres")
    private String nombres;

    @Schema(example = "Quispe Rojas")
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no pueden superar 80 caracteres")
    private String apellidos;

    @Schema(example = "lucia@correo.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    private String correo;

    @Schema(example = "Clave2026")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe tener letras y números")
    private String contrasena;

    /** CONSUMIDOR o COMERCIANTE. El administrador no se puede registrar. */
    @Schema(example = "CONSUMIDOR")
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "CONSUMIDOR|COMERCIANTE", message = "El rol debe ser CONSUMIDOR o COMERCIANTE")
    private String rol;
}

package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Credenciales para iniciar sesión (US03).
 */
@Data
public class LoginDTO {

    @Schema(example = "admin@ecomarket.pe")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @Schema(example = "Admin12345")
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}

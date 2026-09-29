package pe.edu.upc.ecomarket.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInResource(
        @Schema(example = "lucia@correo.com")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,

        @Schema(example = "Clave2026")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}

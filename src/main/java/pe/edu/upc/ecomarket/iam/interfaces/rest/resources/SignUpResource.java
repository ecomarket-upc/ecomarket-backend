package pe.edu.upc.ecomarket.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;

public record SignUpResource(
        @Schema(example = "Lucía")
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 80, message = "Los nombres no pueden superar 80 caracteres")
        String firstName,

        @Schema(example = "Quispe Rojas")
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Los apellidos no pueden superar 80 caracteres")
        String lastName,

        @Schema(example = "lucia@correo.com")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String email,

        @Schema(example = "Clave2026")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe tener al menos una letra y un número")
        String password,

        @Schema(description = "ROLE_CONSUMER o ROLE_SELLER", example = "ROLE_CONSUMER")
        @NotNull(message = "El rol es obligatorio")
        Roles role
) {
}

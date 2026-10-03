package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos para registrar o editar un comercio (US19, US21). Si no se envían latitud y longitud,
 * se calculan a partir de la dirección con el servicio de geocodificación (TS10).
 */
@Data
public class ComercioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @Pattern(regexp = "^[0-9+ ]{6,20}$", message = "El teléfono solo puede tener números, espacios y +")
    private String telefono;

    @Email(message = "El correo de contacto no tiene un formato válido")
    @Size(max = 120, message = "El correo de contacto no puede superar 120 caracteres")
    private String correoContacto;

    @Size(max = 120, message = "El horario no puede superar 120 caracteres")
    private String horario;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 160, message = "La dirección no puede superar 160 caracteres")
    private String direccion;

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 80, message = "El distrito no puede superar 80 caracteres")
    private String distrito;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
    private String ciudad;

    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;
}

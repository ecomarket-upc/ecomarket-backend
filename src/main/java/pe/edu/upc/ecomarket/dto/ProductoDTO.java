package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Datos para publicar o editar un producto (US20). Las Eco-Etiquetas son opcionales:
 * si se envían, quedan como asignadas manualmente (US16).
 */
@Data
public class ProductoDTO {

    /** Comercio del comerciante donde se publica. Al editar no se puede cambiar. */
    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Size(max = 300, message = "La URL de la imagen no puede superar 300 caracteres")
    private String imagenUrl;

    private Set<Long> ecoEtiquetaIds;
}

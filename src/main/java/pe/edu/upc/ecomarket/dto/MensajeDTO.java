package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta simple con un mensaje, por ejemplo al desactivar una Eco-Etiqueta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {

    private String mensaje;
}

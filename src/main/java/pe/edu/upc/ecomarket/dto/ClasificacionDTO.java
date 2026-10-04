package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de POST /api/productos/{id}/clasificar (TS06): el producto con sus Eco-Etiquetas,
 * el motor usado ("gemini" o "keywords") y un mensaje para el comerciante (US13).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionDTO {

    private ProductoRespuestaDTO producto;
    private String motor;
    private String mensaje;
}

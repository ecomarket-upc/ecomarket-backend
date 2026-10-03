package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Producto en la comparación (US12). La distancia se omite si el consumidor no envió su ubicación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoComparadoDTO {

    private ProductoRespuestaDTO producto;
    private Double distanciaKm;
}

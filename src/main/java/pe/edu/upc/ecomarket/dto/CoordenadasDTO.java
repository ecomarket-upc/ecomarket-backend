package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Latitud y longitud obtenidas del servicio de geocodificación (TS10).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasDTO {

    private double latitud;
    private double longitud;
}

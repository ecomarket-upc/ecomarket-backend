package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comercio encontrado por la búsqueda por cercanía y su distancia en km (US05, TS03).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioCercanoDTO {

    private ComercioRespuestaDTO comercio;
    private double distanciaKm;
}

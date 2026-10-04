package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Comercios recomendados (US18, TS12). criterio = "preferencias" o "mas-cercanos".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionComerciosDTO {

    private String criterio;
    private List<ComercioCercanoDTO> comercios;
}

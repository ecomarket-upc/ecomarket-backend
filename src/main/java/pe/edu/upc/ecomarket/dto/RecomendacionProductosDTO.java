package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Productos recomendados (US17, TS12). criterio = "preferencias" o "mas-consultados".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionProductosDTO {

    private String criterio;
    private List<ProductoRespuestaDTO> productos;
}

package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un nombre y cuántas veces aparece: visitas por producto, términos de búsqueda, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConteoDTO {

    private Long id;
    private String nombre;
    private Long cantidad;
}

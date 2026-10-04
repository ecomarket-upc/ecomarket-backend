package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Detalle de un producto: precio, stock, categoría, comercio y Eco-Etiquetas con su significado (US11, US14).
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoRespuestaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
    private Long comercioId;
    private String comercioNombre;
    private Long categoriaId;
    private String categoriaNombre;
    private List<EcoEtiquetaDTO> ecoEtiquetas;
    /** "gemini", "keywords" o "manual"; nulo si aún no se clasificó. */
    private String motorClasificacion;
    /** Producto de un comercio con plan premium activo: aparece primero (US25). */
    private boolean destacado;
    /** Aviso cuando el producto no tiene Eco-Etiquetas (US14, escenario 2). */
    private String avisoEcoEtiquetas;
}

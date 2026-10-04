package pe.edu.upc.ecomarket.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Característica sostenible oficial, por ejemplo "Venta a granel" (US14, US28, TS05).
 * Las palabras clave se usan cuando Gemini no está disponible (TS06).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "eco_etiquetas")
public class EcoEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    /** Qué debe cumplir un producto para llevar esta Eco-Etiqueta. */
    @Column(length = 500)
    private String criterio;

    /** Palabras clave separadas por comas, por ejemplo "granel, por kilo, trae tu envase". */
    @Column(length = 500)
    private String palabrasClave;

    /** Falso cuando el administrador la elimina y ya estaba asignada a productos (eliminación lógica). */
    @Column(nullable = false)
    private boolean activa = true;

    public EcoEtiqueta(String nombre, String descripcion, String criterio, String palabrasClave) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.criterio = criterio;
        this.palabrasClave = palabrasClave;
    }
}

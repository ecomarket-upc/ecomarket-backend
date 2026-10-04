package pe.edu.upc.ecomarket.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Consulta al detalle de un comercio o de uno de sus productos (US22, US23, US31, TS13).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;

    /** Nulo cuando la visita fue al comercio y no a un producto. */
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /** Nulo cuando visita alguien sin sesión. */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Término de búsqueda que llevó al comercio, si lo hubo (US23). */
    @Column(length = 100)
    private String terminoBusqueda;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}

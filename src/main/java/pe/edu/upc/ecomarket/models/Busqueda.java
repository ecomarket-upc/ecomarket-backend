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
 * Término que un usuario buscó. Se usa para las recomendaciones (US17) y las estadísticas (TS13).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "busquedas")
public class Busqueda {

    public static final String TIPO_PRODUCTO = "PRODUCTO";
    public static final String TIPO_COMERCIO = "COMERCIO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nulo cuando busca un visitante sin sesión. */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String termino;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}

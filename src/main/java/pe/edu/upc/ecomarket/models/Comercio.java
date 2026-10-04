package pe.edu.upc.ecomarket.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Establecimiento registrado por un comerciante (US19, US21, US30, TS02, TS10).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comercios")
public class Comercio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 120)
    private String correoContacto;

    @Column(length = 120)
    private String horario;

    @Column(nullable = false, length = 160)
    private String direccion;

    @Column(nullable = false, length = 80)
    private String distrito;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoComercio estado = EstadoComercio.PENDING;

    /** Motivo indicado por el administrador cuando rechaza el comercio (US30). */
    @Column(length = 300)
    private String motivoRechazo;

    /** Comerciante dueño del establecimiento. */
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario propietario;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}

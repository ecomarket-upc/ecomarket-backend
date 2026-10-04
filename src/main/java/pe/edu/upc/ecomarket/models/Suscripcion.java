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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Plan premium contratado para un comercio (US24, TS14). El pago es simulado.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "suscripciones")
public class Suscripcion {

    public static final String PLAN_PREMIUM = "PREMIUM";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;

    @Column(nullable = false, length = 20)
    private String plan = PLAN_PREMIUM;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    /** El plan está activo hasta su fecha de fin, inclusive (US24, escenario 2). */
    public boolean estaActiva() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }
}

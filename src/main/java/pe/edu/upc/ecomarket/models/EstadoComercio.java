package pe.edu.upc.ecomarket.models;

/**
 * Estado de validación del comercio. Se usan los valores que indica TS02 del informe
 * (PENDING, APPROVED, REJECTED). Solo los comercios APPROVED aparecen en las búsquedas.
 */
public enum EstadoComercio {
    PENDING,
    APPROVED,
    REJECTED
}

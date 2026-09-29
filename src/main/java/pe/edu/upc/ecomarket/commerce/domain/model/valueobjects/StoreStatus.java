package pe.edu.upc.ecomarket.commerce.domain.model.valueobjects;

/**
 * Validation state of a store. Only approved stores are visible to consumers.
 */
public enum StoreStatus {
    PENDING,
    APPROVED,
    REJECTED
}

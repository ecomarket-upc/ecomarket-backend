package pe.edu.upc.ecomarket.commerce.domain.model.events;

/**
 * Published when a store is deleted, so other contexts (catalog) can remove what belongs to it.
 */
public record StoreDeletedEvent(Long storeId) {
}

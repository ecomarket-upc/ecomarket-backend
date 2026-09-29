package pe.edu.upc.ecomarket.catalog.domain.model.queries;

/**
 * Products of every store owned by the user.
 */
public record GetProductsByOwnerIdQuery(Long ownerId) {
}

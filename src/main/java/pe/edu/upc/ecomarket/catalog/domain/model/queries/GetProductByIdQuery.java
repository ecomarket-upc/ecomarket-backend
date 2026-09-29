package pe.edu.upc.ecomarket.catalog.domain.model.queries;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

/**
 * @param requester null for anonymous requests; products of non approved stores are only
 *                  visible to the store owner or an admin
 */
public record GetProductByIdQuery(Long productId, Requester requester) {
}

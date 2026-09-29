package pe.edu.upc.ecomarket.commerce.domain.model.queries;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

/**
 * @param requester null for anonymous requests; non approved stores are only visible to their owner or an admin
 */
public record GetStoreByIdQuery(Long storeId, Requester requester) {
}

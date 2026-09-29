package pe.edu.upc.ecomarket.commerce.domain.model.queries;

/**
 * @param district optional filter; null returns every approved store
 */
public record GetApprovedStoresQuery(String district) {
}

package pe.edu.upc.ecomarket.catalog.domain.model.queries;

/**
 * Public product search. Every filter is optional; only products of approved stores are returned.
 *
 * @param name part of the product name, case insensitive
 */
public record GetProductsQuery(Long storeId, Long categoryId, Long ecoLabelId, String name) {

    public GetProductsQuery {
        name = name == null || name.isBlank() ? null : name.trim().toLowerCase();
    }
}

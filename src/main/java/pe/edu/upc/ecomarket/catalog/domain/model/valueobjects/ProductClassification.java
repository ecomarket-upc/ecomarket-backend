package pe.edu.upc.ecomarket.catalog.domain.model.valueobjects;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;

/**
 * Result of classifying a product.
 *
 * @param engine which classifier produced the labels: {@code gemini} or {@code keywords}
 */
public record ProductClassification(Product product, String engine) {
}

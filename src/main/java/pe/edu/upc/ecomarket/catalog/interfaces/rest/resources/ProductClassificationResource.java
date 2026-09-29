package pe.edu.upc.ecomarket.catalog.interfaces.rest.resources;

/**
 * @param engine {@code gemini} when Google Gemini classified the product, {@code keywords} for the offline classifier
 */
public record ProductClassificationResource(ProductResource product, String engine) {
}

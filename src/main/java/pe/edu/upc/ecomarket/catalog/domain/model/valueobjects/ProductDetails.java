package pe.edu.upc.ecomarket.catalog.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Editable data of a product.
 */
public record ProductDetails(Long categoryId, String name, String description, BigDecimal price, int stock,
                             String imageUrl) {

    public ProductDetails {
        if (categoryId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        name = name.trim();
    }
}

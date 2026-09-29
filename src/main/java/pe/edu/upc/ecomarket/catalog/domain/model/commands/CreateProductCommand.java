package pe.edu.upc.ecomarket.catalog.domain.model.commands;

import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductDetails;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.Set;

/**
 * @param storeId store that publishes the product; it must belong to the requester
 */
public record CreateProductCommand(Long storeId, ProductDetails details, Set<Long> ecoLabelIds, Requester requester) {

    public CreateProductCommand {
        if (storeId == null) {
            throw new IllegalArgumentException("El comercio es obligatorio");
        }
    }
}

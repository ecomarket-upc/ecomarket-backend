package pe.edu.upc.ecomarket.catalog.domain.model.commands;

import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductDetails;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.Set;

public record UpdateProductCommand(Long productId, ProductDetails details, Set<Long> ecoLabelIds, Requester requester) {
}

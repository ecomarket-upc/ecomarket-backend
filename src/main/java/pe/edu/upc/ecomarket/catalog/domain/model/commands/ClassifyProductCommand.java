package pe.edu.upc.ecomarket.catalog.domain.model.commands;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

/**
 * Analyzes the product name and description and replaces its eco labels with the suggested ones.
 */
public record ClassifyProductCommand(Long productId, Requester requester) {
}

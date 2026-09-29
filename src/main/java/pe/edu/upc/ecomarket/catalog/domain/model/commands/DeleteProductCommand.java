package pe.edu.upc.ecomarket.catalog.domain.model.commands;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

public record DeleteProductCommand(Long productId, Requester requester) {
}

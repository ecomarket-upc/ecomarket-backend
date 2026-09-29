package pe.edu.upc.ecomarket.commerce.domain.model.commands;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

public record DeleteStoreCommand(Long storeId, Requester requester) {
}

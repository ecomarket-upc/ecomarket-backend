package pe.edu.upc.ecomarket.commerce.domain.model.commands;

import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;

/**
 * Administrator decision about a store: APPROVED or REJECTED (a rejection needs a comment).
 */
public record ValidateStoreCommand(Long storeId, StoreStatus decision, String comment) {

    public ValidateStoreCommand {
        if (decision == null || decision == StoreStatus.PENDING) {
            throw new IllegalArgumentException("La decisión debe ser APPROVED o REJECTED");
        }
    }
}

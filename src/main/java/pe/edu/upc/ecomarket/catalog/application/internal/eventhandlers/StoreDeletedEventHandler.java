package pe.edu.upc.ecomarket.catalog.application.internal.eventhandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import pe.edu.upc.ecomarket.commerce.domain.model.events.StoreDeletedEvent;

/**
 * When a store is deleted its products are deleted too. It runs inside the same transaction
 * as the store deletion, so both succeed or fail together.
 */
@Component
@RequiredArgsConstructor
public class StoreDeletedEventHandler {

    private final ProductRepository productRepository;

    @EventListener
    public void on(StoreDeletedEvent event) {
        productRepository.deleteAll(productRepository.findAllByStoreId(event.storeId()));
    }
}

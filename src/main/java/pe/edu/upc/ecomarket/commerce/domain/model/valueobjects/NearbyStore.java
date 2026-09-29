package pe.edu.upc.ecomarket.commerce.domain.model.valueobjects;

import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;

/**
 * A store found by the proximity search and its distance to the searched point.
 */
public record NearbyStore(Store store, double distanceKm) {
}

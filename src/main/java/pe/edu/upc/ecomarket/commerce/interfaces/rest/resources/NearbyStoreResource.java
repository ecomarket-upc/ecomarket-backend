package pe.edu.upc.ecomarket.commerce.interfaces.rest.resources;

/**
 * @param distanceKm distance from the searched point, rounded to two decimals
 */
public record NearbyStoreResource(StoreResource store, double distanceKm) {
}

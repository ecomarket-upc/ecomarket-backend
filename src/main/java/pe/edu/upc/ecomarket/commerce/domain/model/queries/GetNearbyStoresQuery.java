package pe.edu.upc.ecomarket.commerce.domain.model.queries;

import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;

/**
 * Approved stores within {@code radiusKm} of {@code origin}, closest first.
 */
public record GetNearbyStoresQuery(GeoLocation origin, double radiusKm) {

    public static final double MAX_RADIUS_KM = 50;

    public GetNearbyStoresQuery {
        if (radiusKm <= 0 || radiusKm > MAX_RADIUS_KM) {
            throw new IllegalArgumentException("El radio debe ser mayor que 0 y como máximo 50 km");
        }
    }
}

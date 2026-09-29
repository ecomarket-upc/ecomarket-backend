package pe.edu.upc.ecomarket.commerce.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Latitude and longitude in decimal degrees (WGS84).
 */
@Embeddable
public record GeoLocation(
        @Column(nullable = false) Double latitude,
        @Column(nullable = false) Double longitude
) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public GeoLocation {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("La latitud y la longitud son obligatorias");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
        }
    }

    /**
     * Great-circle distance in kilometers (Haversine formula).
     */
    public double distanceTo(GeoLocation other) {
        double dLat = Math.toRadians(other.latitude - latitude);
        double dLng = Math.toRadians(other.longitude - longitude);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }
}

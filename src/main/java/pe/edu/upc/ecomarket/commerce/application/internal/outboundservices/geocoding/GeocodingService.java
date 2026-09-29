package pe.edu.upc.ecomarket.commerce.application.internal.outboundservices.geocoding;

import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.Address;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;

import java.util.Optional;

/**
 * Converts a postal address into coordinates.
 */
public interface GeocodingService {

    /**
     * @return the coordinates of the address, or empty if it cannot be found or the provider is unavailable
     */
    Optional<GeoLocation> geocode(Address address);
}

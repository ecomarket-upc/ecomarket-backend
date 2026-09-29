package pe.edu.upc.ecomarket.commerce.infrastructure.geocoding.nominatim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.ecomarket.commerce.application.internal.outboundservices.geocoding.GeocodingService;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.Address;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;

import java.util.List;
import java.util.Optional;

/**
 * Geocoding with OpenStreetMap Nominatim: free and without an API key. Its usage policy
 * requires an identifying User-Agent and at most one request per second, which is enough
 * because it is only called when a store is created or edited without coordinates.
 */
@Slf4j
@Service
public class NominatimGeocodingService implements GeocodingService {

    private final RestClient restClient;
    private final boolean enabled;

    public NominatimGeocodingService(@Value("${app.geocoding.enabled}") boolean enabled,
                                     @Value("${app.geocoding.nominatim-url}") String baseUrl,
                                     @Value("${app.geocoding.user-agent}") String userAgent) {
        this.enabled = enabled;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    public Optional<GeoLocation> geocode(Address address) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            List<NominatimPlace> places = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("q", address.toQuery())
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            if (places == null || places.isEmpty()) {
                return Optional.empty();
            }
            NominatimPlace place = places.getFirst();
            return Optional.of(new GeoLocation(Double.valueOf(place.lat()), Double.valueOf(place.lon())));
        } catch (RestClientException | IllegalArgumentException ex) {
            log.warn("Geocoding failed for '{}': {}", address.toQuery(), ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fields of a Nominatim search result used by the application.
     */
    record NominatimPlace(String lat, String lon) {
    }
}

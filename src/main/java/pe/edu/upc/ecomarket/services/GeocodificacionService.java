package pe.edu.upc.ecomarket.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.ecomarket.dto.CoordenadasDTO;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Convierte una dirección en latitud y longitud con OpenStreetMap Nominatim (TS10).
 * Es gratis y no necesita API key; su política pide identificarse con un User-Agent.
 */
@Slf4j
@Service
public class GeocodificacionService {

    private final RestClient restClient;
    private final boolean habilitada;

    public GeocodificacionService(@Value("${geocodificacion.habilitada}") boolean habilitada,
                                  @Value("${geocodificacion.url}") String url,
                                  @Value("${geocodificacion.agente}") String agente) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(5));
        tiempos.setReadTimeout(Duration.ofSeconds(5));
        this.habilitada = habilitada;
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(tiempos)
                .defaultHeader("User-Agent", agente)
                .build();
    }

    /**
     * @return las coordenadas de la dirección, o vacío si no se encontró o el servicio no respondió
     */
    public Optional<CoordenadasDTO> geocodificar(String direccion, String distrito, String ciudad) {
        if (!habilitada) {
            return Optional.empty();
        }
        String consulta = direccion + ", " + distrito + ", " + ciudad;
        try {
            List<LugarNominatim> lugares = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("q", consulta)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            if (lugares == null || lugares.isEmpty()) {
                return Optional.empty();
            }
            LugarNominatim lugar = lugares.getFirst();
            return Optional.of(new CoordenadasDTO(Double.parseDouble(lugar.lat()), Double.parseDouble(lugar.lon())));
        } catch (RestClientException | NumberFormatException e) {
            log.warn("No se pudo geocodificar '{}': {}", consulta, e.getMessage());
            return Optional.empty();
        }
    }

    /** Campos que se usan de la respuesta de Nominatim. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record LugarNominatim(String lat, String lon) {
    }
}

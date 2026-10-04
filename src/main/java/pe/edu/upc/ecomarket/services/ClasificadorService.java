package pe.edu.upc.ecomarket.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;

import java.text.Normalizer;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sugiere las Eco-Etiquetas de un producto (US13, TS06).
 *
 * <pre>
 * Producto → Spring Boot → Gemini → Eco-Etiquetas sugeridas → Producto
 * </pre>
 * Si no hay GEMINI_API_KEY, o Gemini falla o supera el tiempo de espera, usa las palabras clave
 * que definió el administrador para cada Eco-Etiqueta (US28).
 */
@Slf4j
@Service
public class ClasificadorService {

    public static final String MOTOR_GEMINI = "gemini";
    public static final String MOTOR_KEYWORDS = "keywords";
    private static final Pattern NUMERO = Pattern.compile("\\d+");

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public ClasificadorService(@Value("${gemini.api-key}") String apiKey,
                               @Value("${gemini.modelo}") String modelo,
                               @Value("${gemini.url}") String url,
                               @Value("${gemini.tiempo-espera-segundos}") int tiempoEspera) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(tiempoEspera));
        tiempos.setReadTimeout(Duration.ofSeconds(tiempoEspera));
        this.apiKey = apiKey;
        this.modelo = modelo;
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(tiempos).build();
    }

    /**
     * @param texto     nombre y descripción del producto
     * @param etiquetas Eco-Etiquetas activas entre las que se puede elegir
     */
    public Resultado clasificar(String texto, List<EcoEtiqueta> etiquetas) {
        if (apiKey != null && !apiKey.isBlank() && !etiquetas.isEmpty()) {
            Optional<Set<EcoEtiqueta>> conGemini = consultarGemini(texto, etiquetas);
            if (conGemini.isPresent()) {
                return new Resultado(conGemini.get(), MOTOR_GEMINI);
            }
        }
        return new Resultado(porPalabrasClave(texto, etiquetas), MOTOR_KEYWORDS);
    }

    /** Eco-Etiquetas elegidas y motor usado: "gemini" o "keywords". */
    public record Resultado(Set<EcoEtiqueta> ecoEtiquetas, String motor) {
    }

    // ---------------------------------------------------------------- Gemini

    private Optional<Set<EcoEtiqueta>> consultarGemini(String texto, List<EcoEtiqueta> etiquetas) {
        try {
            Map<String, Object> cuerpo = Map.of("contents",
                    List.of(Map.of("parts", List.of(Map.of("text", armarPrompt(texto, etiquetas))))));
            RespuestaGemini respuesta = restClient.post()
                    .uri("/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(RespuestaGemini.class);
            String textoRespuesta = respuesta == null ? null : respuesta.primerTexto();
            if (textoRespuesta == null) {
                return Optional.empty();
            }
            return Optional.of(idsElegidos(textoRespuesta, etiquetas));
        } catch (RestClientException e) {
            log.warn("Gemini no respondió, se usan las palabras clave: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static String armarPrompt(String texto, List<EcoEtiqueta> etiquetas) {
        String lista = etiquetas.stream()
                .map(e -> "- id " + e.getId() + ": " + e.getNombre() + ". " + valor(e.getDescripcion())
                        + " Criterio: " + valor(e.getCriterio()))
                .collect(Collectors.joining("\n"));
        return """
                Eres un clasificador de sostenibilidad para una plataforma de comercio local en Perú.
                Lee el producto y elige solo las Eco-Etiquetas que el texto respalda claramente.

                Eco-Etiquetas disponibles:
                %s

                Producto: %s

                Responde únicamente con los ids elegidos separados por comas (por ejemplo: 1,4).
                Si ninguna aplica, responde NINGUNA.
                """.formatted(lista, texto);
    }

    /** Toma solo los números que son ids de Eco-Etiquetas disponibles. */
    private static Set<EcoEtiqueta> idsElegidos(String respuesta, List<EcoEtiqueta> etiquetas) {
        Map<Long, EcoEtiqueta> porId = etiquetas.stream().collect(Collectors.toMap(EcoEtiqueta::getId, e -> e));
        Set<EcoEtiqueta> elegidas = new HashSet<>();
        Matcher matcher = NUMERO.matcher(respuesta);
        while (matcher.find()) {
            EcoEtiqueta etiqueta = porId.get(Long.valueOf(matcher.group()));
            if (etiqueta != null) {
                elegidas.add(etiqueta);
            }
        }
        return elegidas;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaGemini(List<Candidato> candidates) {

        String primerTexto() {
            if (candidates == null || candidates.isEmpty() || candidates.getFirst().content() == null
                    || candidates.getFirst().content().parts() == null) {
                return null;
            }
            return candidates.getFirst().content().parts().stream()
                    .map(Parte::text)
                    .filter(t -> t != null)
                    .collect(Collectors.joining(" "));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidato(Contenido content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Contenido(List<Parte> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Parte(String text) {
    }

    // ---------------------------------------------------------------- Palabras clave

    /**
     * Una Eco-Etiqueta aplica si el texto contiene su nombre o alguna de sus palabras clave.
     * Se ignoran tildes, mayúsculas, género y plural ("orgánica" = "organico").
     */
    public Set<EcoEtiqueta> porPalabrasClave(String texto, List<EcoEtiqueta> etiquetas) {
        String normalizado = normalizar(texto);
        Set<EcoEtiqueta> elegidas = new HashSet<>();
        for (EcoEtiqueta etiqueta : etiquetas) {
            boolean coincideNombre = normalizado.contains(normalizar(etiqueta.getNombre()));
            boolean coincidePalabra = palabras(etiqueta.getPalabrasClave()).stream()
                    .map(ClasificadorService::normalizar)
                    .anyMatch(normalizado::contains);
            if (coincideNombre || coincidePalabra) {
                elegidas.add(etiqueta);
            }
        }
        return elegidas;
    }

    private static List<String> palabras(String palabrasClave) {
        if (palabrasClave == null || palabrasClave.isBlank()) {
            return List.of();
        }
        return List.of(palabrasClave.split(",")).stream().map(String::trim).filter(p -> !p.isEmpty()).toList();
    }

    static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("(?<=\\p{L}{3})(os|as|es|o|a)\\b", "");
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }
}

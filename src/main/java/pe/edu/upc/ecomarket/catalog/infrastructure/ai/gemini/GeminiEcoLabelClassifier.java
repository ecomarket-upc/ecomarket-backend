package pe.edu.upc.ecomarket.catalog.infrastructure.ai.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.ecomarket.catalog.application.internal.outboundservices.ai.EcoLabelClassifier;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;
import pe.edu.upc.ecomarket.catalog.infrastructure.ai.keywords.KeywordEcoLabelMatcher;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Classifies products with Google Gemini. Without GEMINI_API_KEY, or if Gemini fails, it falls
 * back to {@link KeywordEcoLabelMatcher} so the feature always works in a demo.
 */
@Slf4j
@Service
public class GeminiEcoLabelClassifier implements EcoLabelClassifier {

    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private final KeywordEcoLabelMatcher keywordMatcher;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiEcoLabelClassifier(KeywordEcoLabelMatcher keywordMatcher,
                                    @Value("${app.gemini.api-key}") String apiKey,
                                    @Value("${app.gemini.model}") String model,
                                    @Value("${app.gemini.url}") String baseUrl) {
        this.keywordMatcher = keywordMatcher;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Suggestion classify(String productText, List<EcoLabel> availableLabels) {
        if (apiKey != null && !apiKey.isBlank() && !availableLabels.isEmpty()) {
            Optional<Set<Long>> fromGemini = askGemini(productText, availableLabels);
            if (fromGemini.isPresent()) {
                return new Suggestion(fromGemini.get(), "gemini");
            }
        }
        return new Suggestion(keywordMatcher.match(productText, availableLabels), "keywords");
    }

    private Optional<Set<Long>> askGemini(String productText, List<EcoLabel> availableLabels) {
        try {
            GeminiResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text",
                            buildPrompt(productText, availableLabels)))))))
                    .retrieve()
                    .body(GeminiResponse.class);
            return Optional.ofNullable(response).map(GeminiResponse::firstText)
                    .map(text -> parseIds(text, availableLabels));
        } catch (RestClientException ex) {
            log.warn("Gemini classification failed, using keywords instead: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private static String buildPrompt(String productText, List<EcoLabel> availableLabels) {
        String labels = availableLabels.stream()
                .map(label -> "- id " + label.getId() + ": " + label.getName() + ". " + nullToEmpty(label.getDescription())
                        + " Criterio: " + nullToEmpty(label.getCriteria()))
                .collect(Collectors.joining("\n"));
        return """
                Eres un clasificador de sostenibilidad para una tienda de comercio local en Perú.
                Lee el producto y elige solo las eco-etiquetas que el texto respalda claramente.

                Eco-etiquetas disponibles:
                %s

                Producto: %s

                Responde únicamente con los ids elegidos separados por comas (por ejemplo: 1,4).
                Si ninguna aplica, responde NINGUNA.
                """.formatted(labels, productText);
    }

    /**
     * Keeps only numbers that are ids of the available labels, ignoring anything else Gemini wrote.
     */
    private static Set<Long> parseIds(String text, List<EcoLabel> availableLabels) {
        Set<Long> validIds = availableLabels.stream().map(EcoLabel::getId).collect(Collectors.toSet());
        Set<Long> ids = new LinkedHashSet<>();
        Matcher matcher = NUMBER.matcher(text);
        while (matcher.find()) {
            Long id = Long.valueOf(matcher.group());
            if (validIds.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeminiResponse(List<Candidate> candidates) {

        String firstText() {
            if (candidates == null || candidates.isEmpty() || candidates.getFirst().content() == null
                    || candidates.getFirst().content().parts() == null) {
                return null;
            }
            return candidates.getFirst().content().parts().stream()
                    .map(Part::text)
                    .filter(text -> text != null)
                    .collect(Collectors.joining(" "));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {
    }
}

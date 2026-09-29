package pe.edu.upc.ecomarket.catalog.infrastructure.ai.keywords;

import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Offline classifier: a label applies when the product text contains the label name or any
 * of its keywords. Accents, case, gender and plural are ignored.
 */
@Component
public class KeywordEcoLabelMatcher {

    public Set<Long> match(String productText, List<EcoLabel> availableLabels) {
        String text = normalize(productText);
        Set<Long> matches = new LinkedHashSet<>();
        for (EcoLabel label : availableLabels) {
            boolean nameMatches = text.contains(normalize(label.getName()));
            boolean keywordMatches = label.getKeywords().stream()
                    .map(KeywordEcoLabelMatcher::normalize)
                    .anyMatch(text::contains);
            if (nameMatches || keywordMatches) {
                matches.add(label.getId());
            }
        }
        return matches;
    }

    /**
     * Lower case, without accents and without Spanish gender or plural endings, so
     * "orgánica", "orgánicos" and "organico" all become "organic".
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("(?<=\\p{L}{3})(os|as|es|o|a)\\b", "");
    }
}

package pe.edu.upc.ecomarket.catalog.application.internal.outboundservices.ai;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;

import java.util.List;
import java.util.Set;

/**
 * Suggests which eco labels a product deserves from its free text description.
 */
public interface EcoLabelClassifier {

    /**
     * @param productText   name and description of the product
     * @param availableLabels the official eco labels the classifier may choose from
     */
    Suggestion classify(String productText, List<EcoLabel> availableLabels);

    /**
     * @param ecoLabelIds ids chosen among the available labels (may be empty)
     * @param engine      {@code gemini} or {@code keywords}
     */
    record Suggestion(Set<Long> ecoLabelIds, String engine) {
    }
}

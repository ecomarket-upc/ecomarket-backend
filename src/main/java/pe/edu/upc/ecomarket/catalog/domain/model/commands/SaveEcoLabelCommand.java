package pe.edu.upc.ecomarket.catalog.domain.model.commands;

import java.util.Set;

/**
 * Creates an eco label when {@code ecoLabelId} is null; otherwise updates it.
 */
public record SaveEcoLabelCommand(Long ecoLabelId, String name, String description, String criteria,
                                  Set<String> keywords) {
}

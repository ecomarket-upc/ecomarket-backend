package pe.edu.upc.ecomarket.catalog.domain.model.aggregates;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.ecomarket.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Official sustainability attribute of a product, such as "Venta a granel" or "Sin envase plástico".
 * The criteria and keywords guide the automatic classification of products.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "eco_labels")
public class EcoLabel extends AuditableAbstractAggregateRoot<EcoLabel> {

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 300)
    private String description;

    /** What a product must fulfill to carry this label. */
    @Column(length = 500)
    private String criteria;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "eco_label_keywords", joinColumns = @JoinColumn(name = "eco_label_id"))
    @Column(name = "keyword", nullable = false, length = 60)
    private Set<String> keywords = new LinkedHashSet<>();

    public EcoLabel(String name, String description, String criteria, Set<String> keywords) {
        update(name, description, criteria, keywords);
    }

    public void update(String name, String description, String criteria, Set<String> keywords) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la eco-etiqueta es obligatorio");
        }
        this.name = name.trim();
        this.description = description;
        this.criteria = criteria;
        this.keywords.clear();
        if (keywords != null) {
            this.keywords.addAll(keywords.stream()
                    .filter(keyword -> keyword != null && !keyword.isBlank())
                    .map(keyword -> keyword.trim().toLowerCase())
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }
}

package pe.edu.upc.ecomarket.catalog.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.ecomarket.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

/**
 * Product category, such as "Alimentos a granel". A category may belong to a parent
 * category to build the platform hierarchy.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends AuditableAbstractAggregateRoot<Category> {

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 300)
    private String description;

    /** Id of the parent category, or null for a top level category. */
    private Long parentId;

    public Category(String name, String description, Long parentId) {
        update(name, description, parentId);
    }

    public void update(String name, String description, Long parentId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (parentId != null && parentId.equals(getId())) {
            throw new IllegalArgumentException("Una categoría no puede ser su propia categoría padre");
        }
        this.name = name.trim();
        this.description = description;
        this.parentId = parentId;
    }
}

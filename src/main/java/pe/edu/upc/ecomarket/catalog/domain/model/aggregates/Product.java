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
import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductDetails;
import pe.edu.upc.ecomarket.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Product published by a store. The store, category and eco labels are referenced by id
 * because they are other aggregates (the store even lives in another bounded context).
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends AuditableAbstractAggregateRoot<Product> {

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(length = 300)
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_eco_labels", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "eco_label_id", nullable = false)
    private Set<Long> ecoLabelIds = new LinkedHashSet<>();

    /** Whether the current eco labels were assigned by the automatic classifier. */
    @Column(nullable = false)
    private boolean classifiedByAi;

    public Product(Long storeId, ProductDetails details, Set<Long> ecoLabelIds) {
        this.storeId = storeId;
        update(details, ecoLabelIds);
    }

    public void update(ProductDetails details, Set<Long> ecoLabelIds) {
        this.categoryId = details.categoryId();
        this.name = details.name();
        this.description = details.description();
        this.price = details.price();
        this.stock = details.stock();
        this.imageUrl = details.imageUrl();
        replaceEcoLabels(ecoLabelIds, false);
    }

    public void assignEcoLabelsByAi(Set<Long> ecoLabelIds) {
        replaceEcoLabels(ecoLabelIds, true);
    }

    public void removeEcoLabel(Long ecoLabelId) {
        ecoLabelIds.remove(ecoLabelId);
    }

    /**
     * Text the classifier analyzes.
     */
    public String classificationText() {
        return description == null ? name : name + ". " + description;
    }

    private void replaceEcoLabels(Set<Long> newEcoLabelIds, boolean byAi) {
        this.ecoLabelIds.clear();
        if (newEcoLabelIds != null) {
            this.ecoLabelIds.addAll(newEcoLabelIds);
        }
        this.classifiedByAi = byAi;
    }
}

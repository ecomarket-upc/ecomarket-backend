package pe.edu.upc.ecomarket.commerce.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.Address;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;
import pe.edu.upc.ecomarket.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

/**
 * Local business registered by a seller. It starts as PENDING and becomes visible to
 * consumers once an administrator approves it.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "stores")
public class Store extends AuditableAbstractAggregateRoot<Store> {

    /** Id of the IAM user who owns the store. */
    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    private String phone;

    @Column(length = 120)
    private String contactEmail;

    @Column(length = 120)
    private String openingHours;

    @Embedded
    private Address address;

    @Embedded
    private GeoLocation location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreStatus status;

    @Column(length = 300)
    private String validationComment;

    public Store(Long ownerId, StoreProfile profile, Address address, GeoLocation location) {
        this.ownerId = ownerId;
        this.status = StoreStatus.PENDING;
        update(profile, address, location);
    }

    /**
     * A rejected store goes back to PENDING after being edited, so the administrator can review it again.
     */
    public void update(StoreProfile profile, Address address, GeoLocation location) {
        this.name = profile.name();
        this.description = profile.description();
        this.phone = profile.phone();
        this.contactEmail = profile.contactEmail();
        this.openingHours = profile.openingHours();
        this.address = address;
        this.location = location;
        if (status == StoreStatus.REJECTED) {
            this.status = StoreStatus.PENDING;
            this.validationComment = null;
        }
    }

    public void approve(String comment) {
        this.status = StoreStatus.APPROVED;
        this.validationComment = comment;
    }

    public void reject(String comment) {
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo del rechazo");
        }
        this.status = StoreStatus.REJECTED;
        this.validationComment = comment.trim();
    }

    public boolean isApproved() {
        return status == StoreStatus.APPROVED;
    }

    /**
     * Descriptive data of a store that the seller can edit.
     */
    public record StoreProfile(String name, String description, String phone, String contactEmail,
                               String openingHours) {

        public StoreProfile {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("El nombre del comercio es obligatorio");
            }
            name = name.trim();
        }
    }
}

package pe.edu.upc.ecomarket.commerce.interfaces.rest.resources;

import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;

import java.time.Instant;

public record StoreResource(
        Long id,
        Long ownerId,
        String name,
        String description,
        String phone,
        String contactEmail,
        String openingHours,
        String addressLine,
        String district,
        String city,
        Double latitude,
        Double longitude,
        StoreStatus status,
        String validationComment,
        Instant createdAt
) {
}

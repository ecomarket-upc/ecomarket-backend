package pe.edu.upc.ecomarket.commerce.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Postal address of a store, as typed by the seller.
 */
@Embeddable
public record Address(
        @Column(name = "address_line", nullable = false, length = 160) String addressLine,
        @Column(nullable = false, length = 80) String district,
        @Column(nullable = false, length = 80) String city
) {

    public Address {
        if (addressLine == null || addressLine.isBlank() || district == null || district.isBlank()
                || city == null || city.isBlank()) {
            throw new IllegalArgumentException("La dirección, el distrito y la ciudad son obligatorios");
        }
        addressLine = addressLine.trim();
        district = district.trim();
        city = city.trim();
    }

    /**
     * Single line used to geocode the address.
     */
    public String toQuery() {
        return addressLine + ", " + district + ", " + city;
    }
}

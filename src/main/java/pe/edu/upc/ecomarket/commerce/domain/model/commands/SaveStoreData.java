package pe.edu.upc.ecomarket.commerce.domain.model.commands;

/**
 * Fields shared by the create and update commands. Latitude and longitude are optional:
 * when both are missing the address is geocoded.
 */
public record SaveStoreData(String name, String description, String phone, String contactEmail,
                            String openingHours, String addressLine, String district, String city,
                            Double latitude, Double longitude) {

    public SaveStoreData {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Envía la latitud y la longitud juntas, o ninguna de las dos");
        }
    }

    public boolean hasCoordinates() {
        return latitude != null;
    }
}

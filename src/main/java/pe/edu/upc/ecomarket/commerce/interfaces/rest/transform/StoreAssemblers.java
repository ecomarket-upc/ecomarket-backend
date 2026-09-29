package pe.edu.upc.ecomarket.commerce.interfaces.rest.transform;

import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.SaveStoreData;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.UpdateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.ValidateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.NearbyStore;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.NearbyStoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.SaveStoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.StoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.ValidateStoreResource;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

/**
 * Converts between REST resources and commerce commands or aggregates.
 */
public final class StoreAssemblers {

    private StoreAssemblers() {
    }

    public static CreateStoreCommand toCreateCommand(SaveStoreResource resource, Requester requester) {
        return new CreateStoreCommand(toData(resource), requester);
    }

    public static UpdateStoreCommand toUpdateCommand(Long storeId, SaveStoreResource resource, Requester requester) {
        return new UpdateStoreCommand(storeId, toData(resource), requester);
    }

    public static ValidateStoreCommand toCommand(Long storeId, ValidateStoreResource resource) {
        return new ValidateStoreCommand(storeId, resource.status(), resource.comment());
    }

    public static StoreResource toResource(Store store) {
        return new StoreResource(store.getId(), store.getOwnerId(), store.getName(), store.getDescription(),
                store.getPhone(), store.getContactEmail(), store.getOpeningHours(),
                store.getAddress().addressLine(), store.getAddress().district(), store.getAddress().city(),
                store.getLocation().latitude(), store.getLocation().longitude(),
                store.getStatus(), store.getValidationComment(), store.getCreatedAt());
    }

    public static NearbyStoreResource toResource(NearbyStore nearbyStore) {
        double rounded = Math.round(nearbyStore.distanceKm() * 100) / 100.0;
        return new NearbyStoreResource(toResource(nearbyStore.store()), rounded);
    }

    private static SaveStoreData toData(SaveStoreResource resource) {
        return new SaveStoreData(resource.name(), resource.description(), resource.phone(), resource.contactEmail(),
                resource.openingHours(), resource.addressLine(), resource.district(), resource.city(),
                resource.latitude(), resource.longitude());
    }
}

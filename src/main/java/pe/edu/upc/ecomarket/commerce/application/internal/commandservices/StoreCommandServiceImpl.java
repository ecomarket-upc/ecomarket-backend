package pe.edu.upc.ecomarket.commerce.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.commerce.application.internal.outboundservices.geocoding.GeocodingService;
import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store.StoreProfile;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.DeleteStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.SaveStoreData;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.UpdateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.ValidateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.events.StoreDeletedEvent;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.Address;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;
import pe.edu.upc.ecomarket.commerce.domain.services.StoreCommandService;
import pe.edu.upc.ecomarket.commerce.infrastructure.persistence.jpa.repositories.StoreRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ForbiddenOperationException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

@Service
@RequiredArgsConstructor
public class StoreCommandServiceImpl implements StoreCommandService {

    private final StoreRepository storeRepository;
    private final GeocodingService geocodingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Store handle(CreateStoreCommand command) {
        SaveStoreData data = command.data();
        Address address = toAddress(data);
        Store store = new Store(command.requester().userId(), toProfile(data), address, locate(data, address));
        return storeRepository.save(store);
    }

    @Override
    @Transactional
    public Store handle(UpdateStoreCommand command) {
        Store store = findOwnedStore(command.storeId(), command.requester(), "Solo puedes editar tus propios comercios");
        SaveStoreData data = command.data();
        Address address = toAddress(data);
        store.update(toProfile(data), address, locate(data, address));
        return storeRepository.save(store);
    }

    @Override
    @Transactional
    public void handle(DeleteStoreCommand command) {
        Store store = findOwnedStore(command.storeId(), command.requester(), "Solo puedes eliminar tus propios comercios");
        eventPublisher.publishEvent(new StoreDeletedEvent(store.getId()));
        storeRepository.delete(store);
    }

    @Override
    @Transactional
    public Store handle(ValidateStoreCommand command) {
        Store store = findStore(command.storeId());
        if (command.decision() == StoreStatus.APPROVED) {
            store.approve(command.comment());
        } else {
            store.reject(command.comment());
        }
        return storeRepository.save(store);
    }

    /**
     * Uses the coordinates sent by the client; otherwise geocodes the address.
     */
    private GeoLocation locate(SaveStoreData data, Address address) {
        if (data.hasCoordinates()) {
            return new GeoLocation(data.latitude(), data.longitude());
        }
        return geocodingService.geocode(address)
                .orElseThrow(() -> new BusinessRuleException(
                        "No se pudo ubicar la dirección. Envía la latitud y la longitud del comercio"));
    }

    private Store findOwnedStore(Long storeId, Requester requester, String forbiddenMessage) {
        Store store = findStore(storeId);
        if (!requester.canManage(store.getOwnerId())) {
            throw new ForbiddenOperationException(forbiddenMessage);
        }
        return store;
    }

    private Store findStore(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new ResourceNotFoundException("Comercio", storeId));
    }

    private static Address toAddress(SaveStoreData data) {
        return new Address(data.addressLine(), data.district(), data.city());
    }

    private static StoreProfile toProfile(SaveStoreData data) {
        return new StoreProfile(data.name(), data.description(), data.phone(), data.contactEmail(), data.openingHours());
    }
}

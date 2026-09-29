package pe.edu.upc.ecomarket.commerce.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetApprovedStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetNearbyStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetPendingStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetStoresByOwnerIdQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.NearbyStore;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;
import pe.edu.upc.ecomarket.commerce.domain.services.StoreQueryService;
import pe.edu.upc.ecomarket.commerce.infrastructure.persistence.jpa.repositories.StoreRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryServiceImpl implements StoreQueryService {

    /** Approximate kilometers per degree of latitude, used for the bounding box. */
    private static final double KM_PER_DEGREE = 111.32;

    private final StoreRepository storeRepository;

    @Override
    public List<Store> handle(GetApprovedStoresQuery query) {
        if (query.district() == null || query.district().isBlank()) {
            return storeRepository.findAllByStatusOrderByIdAsc(StoreStatus.APPROVED);
        }
        return storeRepository.findAllByStatusAndAddressDistrictIgnoreCaseOrderByIdAsc(
                StoreStatus.APPROVED, query.district().trim());
    }

    /**
     * A store that is not approved yet is reported as missing to anyone but its owner or an admin.
     */
    @Override
    public Store handle(GetStoreByIdQuery query) {
        return storeRepository.findById(query.storeId())
                .filter(store -> store.isApproved()
                        || (query.requester() != null && query.requester().canManage(store.getOwnerId())))
                .orElseThrow(() -> new ResourceNotFoundException("Comercio", query.storeId()));
    }

    @Override
    public List<Store> handle(GetStoresByOwnerIdQuery query) {
        return storeRepository.findAllByOwnerIdOrderByIdAsc(query.ownerId());
    }

    @Override
    public List<Store> handle(GetPendingStoresQuery query) {
        return storeRepository.findAllByStatusOrderByIdAsc(StoreStatus.PENDING);
    }

    @Override
    public List<NearbyStore> handle(GetNearbyStoresQuery query) {
        GeoLocation origin = query.origin();
        double latDelta = query.radiusKm() / KM_PER_DEGREE;
        double cosLat = Math.max(Math.cos(Math.toRadians(origin.latitude())), 0.01);
        double lngDelta = query.radiusKm() / (KM_PER_DEGREE * cosLat);
        return storeRepository.findAllByStatusAndLocationLatitudeBetweenAndLocationLongitudeBetween(
                        StoreStatus.APPROVED,
                        origin.latitude() - latDelta, origin.latitude() + latDelta,
                        origin.longitude() - lngDelta, origin.longitude() + lngDelta)
                .stream()
                .map(store -> new NearbyStore(store, origin.distanceTo(store.getLocation())))
                .filter(nearby -> nearby.distanceKm() <= query.radiusKm())
                .sorted(Comparator.comparingDouble(NearbyStore::distanceKm))
                .toList();
    }
}

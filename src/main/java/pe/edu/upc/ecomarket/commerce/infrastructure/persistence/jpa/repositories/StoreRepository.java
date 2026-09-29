package pe.edu.upc.ecomarket.commerce.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByStatusOrderByIdAsc(StoreStatus status);

    List<Store> findAllByStatusAndAddressDistrictIgnoreCaseOrderByIdAsc(StoreStatus status, String district);

    List<Store> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    /**
     * Candidates for the proximity search: stores inside a bounding box around the searched point.
     * The exact distance is computed afterwards with the Haversine formula.
     */
    List<Store> findAllByStatusAndLocationLatitudeBetweenAndLocationLongitudeBetween(
            StoreStatus status, Double minLatitude, Double maxLatitude, Double minLongitude, Double maxLongitude);
}

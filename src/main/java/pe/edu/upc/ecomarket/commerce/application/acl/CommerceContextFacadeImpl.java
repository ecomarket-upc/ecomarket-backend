package pe.edu.upc.ecomarket.commerce.application.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.StoreStatus;
import pe.edu.upc.ecomarket.commerce.infrastructure.persistence.jpa.repositories.StoreRepository;
import pe.edu.upc.ecomarket.commerce.interfaces.acl.CommerceContextFacade;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommerceContextFacadeImpl implements CommerceContextFacade {

    private final StoreRepository storeRepository;

    @Override
    public Optional<Long> fetchStoreOwnerId(Long storeId) {
        return storeRepository.findById(storeId).map(Store::getOwnerId);
    }

    @Override
    public boolean isStoreApproved(Long storeId) {
        return storeRepository.findById(storeId).map(Store::isApproved).orElse(false);
    }

    @Override
    public List<Long> fetchApprovedStoreIds() {
        return storeRepository.findAllByStatusOrderByIdAsc(StoreStatus.APPROVED).stream()
                .map(Store::getId)
                .toList();
    }

    @Override
    public List<Long> fetchStoreIdsByOwnerId(Long ownerId) {
        return storeRepository.findAllByOwnerIdOrderByIdAsc(ownerId).stream()
                .map(Store::getId)
                .toList();
    }
}

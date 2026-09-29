package pe.edu.upc.ecomarket.catalog.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsByOwnerIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.ProductQueryService;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import pe.edu.upc.ecomarket.commerce.interfaces.acl.CommerceContextFacade;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final CommerceContextFacade commerceContextFacade;

    @Override
    public List<Product> handle(GetProductsQuery query) {
        List<Long> approvedStoreIds = commerceContextFacade.fetchApprovedStoreIds();
        if (approvedStoreIds.isEmpty()) {
            return List.of();
        }
        return productRepository.search(approvedStoreIds, query.storeId(), query.categoryId(),
                query.ecoLabelId(), query.name());
    }

    /**
     * Products of a store that is not approved yet are reported as missing to anyone but
     * the store owner or an admin.
     */
    @Override
    public Product handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId())
                .filter(product -> isVisible(product, query.requester()))
                .orElseThrow(() -> new ResourceNotFoundException("Producto", query.productId()));
    }

    @Override
    public List<Product> handle(GetProductsByOwnerIdQuery query) {
        List<Long> storeIds = commerceContextFacade.fetchStoreIdsByOwnerId(query.ownerId());
        if (storeIds.isEmpty()) {
            return List.of();
        }
        return productRepository.findAllByStoreIdInOrderByIdAsc(storeIds);
    }

    private boolean isVisible(Product product, Requester requester) {
        if (commerceContextFacade.isStoreApproved(product.getStoreId())) {
            return true;
        }
        return requester != null && commerceContextFacade.fetchStoreOwnerId(product.getStoreId())
                .map(requester::canManage)
                .orElse(requester.admin());
    }
}

package pe.edu.upc.ecomarket.catalog.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.catalog.application.internal.outboundservices.ai.EcoLabelClassifier;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.ClassifyProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.CreateProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.UpdateProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductClassification;
import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductDetails;
import pe.edu.upc.ecomarket.catalog.domain.services.ProductCommandService;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.EcoLabelRepository;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import pe.edu.upc.ecomarket.commerce.interfaces.acl.CommerceContextFacade;
import pe.edu.upc.ecomarket.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ForbiddenOperationException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EcoLabelRepository ecoLabelRepository;
    private final CommerceContextFacade commerceContextFacade;
    private final EcoLabelClassifier ecoLabelClassifier;

    @Override
    @Transactional
    public Product handle(CreateProductCommand command) {
        checkStoreOwnership(command.storeId(), command.requester(), "Solo puedes publicar productos en tus propios comercios");
        validateReferences(command.details(), command.ecoLabelIds());
        return productRepository.save(new Product(command.storeId(), command.details(), command.ecoLabelIds()));
    }

    @Override
    @Transactional
    public Product handle(UpdateProductCommand command) {
        Product product = findOwnedProduct(command.productId(), command.requester(), "Solo puedes editar tus propios productos");
        validateReferences(command.details(), command.ecoLabelIds());
        product.update(command.details(), command.ecoLabelIds());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void handle(DeleteProductCommand command) {
        Product product = findOwnedProduct(command.productId(), command.requester(), "Solo puedes eliminar tus propios productos");
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductClassification handle(ClassifyProductCommand command) {
        Product product = findOwnedProduct(command.productId(), command.requester(), "Solo puedes clasificar tus propios productos");
        EcoLabelClassifier.Suggestion suggestion = ecoLabelClassifier.classify(
                product.classificationText(), ecoLabelRepository.findAllByOrderByNameAsc());
        product.assignEcoLabelsByAi(suggestion.ecoLabelIds());
        return new ProductClassification(productRepository.save(product), suggestion.engine());
    }

    private Product findOwnedProduct(Long productId, Requester requester, String forbiddenMessage) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
        checkStoreOwnership(product.getStoreId(), requester, forbiddenMessage);
        return product;
    }

    private void checkStoreOwnership(Long storeId, Requester requester, String forbiddenMessage) {
        Long ownerId = commerceContextFacade.fetchStoreOwnerId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Comercio", storeId));
        if (!requester.canManage(ownerId)) {
            throw new ForbiddenOperationException(forbiddenMessage);
        }
    }

    private void validateReferences(ProductDetails details, Set<Long> ecoLabelIds) {
        if (!categoryRepository.existsById(details.categoryId())) {
            throw new ResourceNotFoundException("Categoría", details.categoryId());
        }
        if (ecoLabelIds != null) {
            ecoLabelIds.stream()
                    .filter(id -> !ecoLabelRepository.existsById(id))
                    .findFirst()
                    .ifPresent(id -> {
                        throw new BusinessRuleException("La eco-etiqueta con id " + id + " no existe");
                    });
        }
    }
}

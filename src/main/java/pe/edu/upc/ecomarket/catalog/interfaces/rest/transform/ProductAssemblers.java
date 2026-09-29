package pe.edu.upc.ecomarket.catalog.interfaces.rest.transform;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.CreateProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.UpdateProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductClassification;
import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductDetails;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.CreateProductResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.ProductClassificationResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.ProductResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.UpdateProductResource;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.Set;

public final class ProductAssemblers {

    private ProductAssemblers() {
    }

    public static CreateProductCommand toCommand(CreateProductResource resource, Requester requester) {
        ProductDetails details = new ProductDetails(resource.categoryId(), resource.name(), resource.description(),
                resource.price(), resource.stock(), resource.imageUrl());
        return new CreateProductCommand(resource.storeId(), details, resource.ecoLabelIds(), requester);
    }

    public static UpdateProductCommand toCommand(Long productId, UpdateProductResource resource, Requester requester) {
        ProductDetails details = new ProductDetails(resource.categoryId(), resource.name(), resource.description(),
                resource.price(), resource.stock(), resource.imageUrl());
        return new UpdateProductCommand(productId, details, resource.ecoLabelIds(), requester);
    }

    public static ProductResource toResource(Product product) {
        return new ProductResource(product.getId(), product.getStoreId(), product.getCategoryId(), product.getName(),
                product.getDescription(), product.getPrice(), product.getStock(), product.getImageUrl(),
                Set.copyOf(product.getEcoLabelIds()), product.isClassifiedByAi(), product.getCreatedAt());
    }

    public static ProductClassificationResource toResource(ProductClassification classification) {
        return new ProductClassificationResource(toResource(classification.product()), classification.engine());
    }
}

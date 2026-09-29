package pe.edu.upc.ecomarket.catalog.domain.services;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.ClassifyProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.CreateProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.UpdateProductCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.valueobjects.ProductClassification;

public interface ProductCommandService {

    Product handle(CreateProductCommand command);

    Product handle(UpdateProductCommand command);

    void handle(DeleteProductCommand command);

    ProductClassification handle(ClassifyProductCommand command);
}

package pe.edu.upc.ecomarket.catalog.domain.services;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Category;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteCategoryCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SaveCategoryCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SeedCategoriesCommand;

public interface CategoryCommandService {

    Category handle(SaveCategoryCommand command);

    void handle(DeleteCategoryCommand command);

    void handle(SeedCategoriesCommand command);
}

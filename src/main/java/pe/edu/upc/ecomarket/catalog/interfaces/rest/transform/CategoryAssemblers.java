package pe.edu.upc.ecomarket.catalog.interfaces.rest.transform;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Category;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SaveCategoryCommand;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.CategoryResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.SaveCategoryResource;

public final class CategoryAssemblers {

    private CategoryAssemblers() {
    }

    /**
     * @param categoryId null to create, the id to update
     */
    public static SaveCategoryCommand toCommand(Long categoryId, SaveCategoryResource resource) {
        return new SaveCategoryCommand(categoryId, resource.name(), resource.description(), resource.parentId());
    }

    public static CategoryResource toResource(Category category) {
        return new CategoryResource(category.getId(), category.getName(), category.getDescription(), category.getParentId());
    }
}

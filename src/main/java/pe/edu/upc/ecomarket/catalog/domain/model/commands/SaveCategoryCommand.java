package pe.edu.upc.ecomarket.catalog.domain.model.commands;

/**
 * Creates a category when {@code categoryId} is null; otherwise updates it.
 */
public record SaveCategoryCommand(Long categoryId, String name, String description, Long parentId) {
}

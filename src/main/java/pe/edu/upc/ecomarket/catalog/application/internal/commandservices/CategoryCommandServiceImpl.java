package pe.edu.upc.ecomarket.catalog.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Category;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteCategoryCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SaveCategoryCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SeedCategoriesCommand;
import pe.edu.upc.ecomarket.catalog.domain.services.CategoryCommandService;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ConflictException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private static final List<String[]> BASE_CATEGORIES = List.of(
            new String[]{"Alimentos a granel", "Granos, menestras, frutos secos y otros alimentos vendidos por peso"},
            new String[]{"Frutas y verduras", "Productos frescos de chacra y ferias orgánicas"},
            new String[]{"Cuidado personal", "Jabones, shampoos sólidos, cepillos y cosmética natural"},
            new String[]{"Limpieza del hogar", "Detergentes, desinfectantes y utensilios de limpieza"},
            new String[]{"Artesanías y textiles", "Objetos y prendas elaborados por artesanos locales"},
            new String[]{"Bebidas", "Cafés, infusiones, jugos y bebidas de productores locales"});

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Category handle(SaveCategoryCommand command) {
        if (command.parentId() != null && !categoryRepository.existsById(command.parentId())) {
            throw new ResourceNotFoundException("Categoría padre", command.parentId());
        }
        if (command.categoryId() == null) {
            if (categoryRepository.existsByNameIgnoreCase(command.name().trim())) {
                throw new ConflictException("Ya existe una categoría llamada " + command.name().trim());
            }
            return categoryRepository.save(new Category(command.name(), command.description(), command.parentId()));
        }
        Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", command.categoryId()));
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(command.name().trim(), category.getId())) {
            throw new ConflictException("Ya existe una categoría llamada " + command.name().trim());
        }
        category.update(command.name(), command.description(), command.parentId());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void handle(DeleteCategoryCommand command) {
        Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", command.categoryId()));
        if (categoryRepository.existsByParentId(category.getId())) {
            throw new ConflictException("No se puede eliminar una categoría que tiene subcategorías");
        }
        if (productRepository.existsByCategoryId(category.getId())) {
            throw new ConflictException("No se puede eliminar una categoría que tiene productos");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public void handle(SeedCategoriesCommand command) {
        if (categoryRepository.count() > 0) {
            return;
        }
        BASE_CATEGORIES.forEach(data -> categoryRepository.save(new Category(data[0], data[1], null)));
    }
}

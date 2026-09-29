package pe.edu.upc.ecomarket.catalog.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Category;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetAllCategoriesQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetCategoryByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.CategoryQueryService;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> handle(GetAllCategoriesQuery query) {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Category handle(GetCategoryByIdQuery query) {
        return categoryRepository.findById(query.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", query.categoryId()));
    }
}

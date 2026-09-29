package pe.edu.upc.ecomarket.catalog.domain.services;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Category;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetAllCategoriesQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetCategoryByIdQuery;

import java.util.List;

public interface CategoryQueryService {

    List<Category> handle(GetAllCategoriesQuery query);

    Category handle(GetCategoryByIdQuery query);
}

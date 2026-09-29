package pe.edu.upc.ecomarket.catalog.domain.services;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsByOwnerIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetProductsQuery;

import java.util.List;

public interface ProductQueryService {

    List<Product> handle(GetProductsQuery query);

    Product handle(GetProductByIdQuery query);

    List<Product> handle(GetProductsByOwnerIdQuery query);
}

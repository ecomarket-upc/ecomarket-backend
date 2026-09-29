package pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.Product;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Products of the given stores that match every non null filter.
     *
     * @param name lower case fragment of the product name
     */
    @Query("""
            select distinct p from Product p left join p.ecoLabelIds label
            where p.storeId in :storeIds
              and (:storeId is null or p.storeId = :storeId)
              and (:categoryId is null or p.categoryId = :categoryId)
              and (:ecoLabelId is null or label = :ecoLabelId)
              and (:name is null or lower(p.name) like concat('%', :name, '%'))
            order by p.id asc
            """)
    List<Product> search(@Param("storeIds") Collection<Long> storeIds,
                         @Param("storeId") Long storeId,
                         @Param("categoryId") Long categoryId,
                         @Param("ecoLabelId") Long ecoLabelId,
                         @Param("name") String name);

    List<Product> findAllByStoreIdInOrderByIdAsc(Collection<Long> storeIds);

    List<Product> findAllByStoreId(Long storeId);

    boolean existsByCategoryId(Long categoryId);

    @Query("select p from Product p join p.ecoLabelIds label where label = :ecoLabelId")
    List<Product> findAllByEcoLabelId(@Param("ecoLabelId") Long ecoLabelId);
}

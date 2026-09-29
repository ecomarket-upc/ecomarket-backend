package pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;

import java.util.List;

@Repository
public interface EcoLabelRepository extends JpaRepository<EcoLabel, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<EcoLabel> findAllByOrderByNameAsc();
}

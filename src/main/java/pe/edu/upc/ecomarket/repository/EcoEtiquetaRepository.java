package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;

import java.util.List;

@Repository
public interface EcoEtiquetaRepository extends JpaRepository<EcoEtiqueta, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<EcoEtiqueta> findAllByOrderByNombreAsc();

    /** Solo las activas se ofrecen como filtro y en nuevas clasificaciones (US28). */
    List<EcoEtiqueta> findByActivaTrueOrderByNombreAsc();
}

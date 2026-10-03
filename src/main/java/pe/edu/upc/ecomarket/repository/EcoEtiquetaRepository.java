package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;

@Repository
public interface EcoEtiquetaRepository extends JpaRepository<EcoEtiqueta, Long> {
}

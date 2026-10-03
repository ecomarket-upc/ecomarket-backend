package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Visita;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Long> {

    void deleteByComercioId(Long comercioId);
}

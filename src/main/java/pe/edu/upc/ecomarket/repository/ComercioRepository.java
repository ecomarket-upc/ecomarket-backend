package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Comercio;

@Repository
public interface ComercioRepository extends JpaRepository<Comercio, Long> {
}

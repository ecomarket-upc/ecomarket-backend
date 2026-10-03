package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Busqueda;

@Repository
public interface BusquedaRepository extends JpaRepository<Busqueda, Long> {
}

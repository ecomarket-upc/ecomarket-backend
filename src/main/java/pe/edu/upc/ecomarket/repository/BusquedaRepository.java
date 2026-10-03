package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Busqueda;

import java.util.List;

@Repository
public interface BusquedaRepository extends JpaRepository<Busqueda, Long> {

    /** Últimas búsquedas del usuario, para las recomendaciones (US17). */
    List<Busqueda> findTop20ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}

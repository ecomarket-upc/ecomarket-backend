package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;

import java.util.List;

@Repository
public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    List<Comercio> findByEstadoOrderByIdAsc(EstadoComercio estado);

    List<Comercio> findByEstadoAndDistritoIgnoreCaseOrderByIdAsc(EstadoComercio estado, String distrito);

    List<Comercio> findByPropietarioIdOrderByIdAsc(Long propietarioId);

    /** Candidatos de la búsqueda por cercanía: comercios dentro de un cuadrado alrededor del punto (TS03). */
    List<Comercio> findByEstadoAndLatitudBetweenAndLongitudBetween(EstadoComercio estado, Double latitudMin,
                                                                   Double latitudMax, Double longitudMin,
                                                                   Double longitudMax);

    /** Búsqueda de comercios por texto (US06, escenario 3). */
    List<Comercio> findByEstadoAndNombreContainingIgnoreCaseOrderByNombreAsc(EstadoComercio estado, String texto);
}

package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Producto;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByEcoEtiquetasId(Long ecoEtiquetaId);

    List<Producto> findByComercioId(Long comercioId);

    /** Catálogo de un comercio (US10). */
    List<Producto> findByComercioIdAndActivoTrueOrderByNombreAsc(Long comercioId);

    /** Productos de todos los comercios de un comerciante. */
    List<Producto> findByComercioPropietarioIdAndActivoTrueOrderByIdAsc(Long propietarioId);

    /** Productos publicados de una categoría, para la comparación premium (US27). */
    long countByCategoriaIdAndActivoTrueAndComercioEstado(Long categoriaId, EstadoComercio estado);

    /**
     * Búsqueda pública (US06, US07, US15, TS04): solo productos activos de comercios aprobados.
     * Un texto vacío ("") significa "sin filtro".
     */
    @Query("""
            select distinct p from Producto p left join p.ecoEtiquetas e
            where p.activo = true
              and p.comercio.estado = pe.edu.upc.ecomarket.models.EstadoComercio.APPROVED
              and lower(p.nombre) like lower(concat('%', :nombre, '%'))
              and lower(p.categoria.nombre) like lower(concat('%', :categoria, '%'))
              and (:ecoEtiqueta = '' or (e.activa = true and lower(e.nombre) like lower(concat('%', :ecoEtiqueta, '%'))))
            order by p.nombre
            """)
    List<Producto> buscar(@Param("nombre") String nombre,
                          @Param("categoria") String categoria,
                          @Param("ecoEtiqueta") String ecoEtiqueta);
}

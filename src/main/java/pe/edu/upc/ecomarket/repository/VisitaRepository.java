package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.dto.ConteoDTO;
import pe.edu.upc.ecomarket.models.Visita;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Long> {

    void deleteByComercioId(Long comercioId);

    /** Visitas al detalle del comercio en el periodo (US22). */
    long countByComercioIdAndProductoIsNullAndFechaBetween(Long comercioId, LocalDateTime desde, LocalDateTime hasta);

    /** Visitas por producto del comercio en el periodo (US22). */
    @Query("""
            select new pe.edu.upc.ecomarket.dto.ConteoDTO(p.id, p.nombre, count(v))
            from Visita v join v.producto p
            where v.comercio.id = :comercioId and v.fecha between :desde and :hasta
            group by p.id, p.nombre
            order by count(v) desc
            """)
    List<ConteoDTO> contarPorProducto(@Param("comercioId") Long comercioId,
                                      @Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta);

    /** Términos de búsqueda que llevaron al comercio en el periodo (US23). */
    @Query("""
            select new pe.edu.upc.ecomarket.dto.ConteoDTO(min(v.id), v.terminoBusqueda, count(v))
            from Visita v
            where v.comercio.id = :comercioId and v.terminoBusqueda is not null and v.fecha between :desde and :hasta
            group by v.terminoBusqueda
            order by count(v) desc
            """)
    List<ConteoDTO> contarTerminos(@Param("comercioId") Long comercioId,
                                   @Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta);

    /** Visitas a productos de una categoría en el periodo, para la comparación premium (US27). */
    @Query("""
            select count(v) from Visita v join v.producto p
            where p.categoria.id = :categoriaId and v.fecha between :desde and :hasta
            """)
    long contarPorCategoria(@Param("categoriaId") Long categoriaId,
                            @Param("desde") LocalDateTime desde,
                            @Param("hasta") LocalDateTime hasta);

    List<Visita> findByComercioIdAndFechaAfter(Long comercioId, LocalDateTime desde);

    /** Productos que consultó el consumidor (US31). */
    List<Visita> findByUsuarioIdAndProductoIsNotNull(Long usuarioId);

    /** Productos más consultados de la plataforma (US17, escenario 2). */
    @Query("""
            select new pe.edu.upc.ecomarket.dto.ConteoDTO(p.id, p.nombre, count(v))
            from Visita v join v.producto p
            where p.activo = true and p.comercio.estado = pe.edu.upc.ecomarket.models.EstadoComercio.APPROVED
            group by p.id, p.nombre
            order by count(v) desc
            """)
    List<ConteoDTO> productosMasConsultados();
}

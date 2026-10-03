package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Favorito;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    void deleteByComercioId(Long comercioId);
}

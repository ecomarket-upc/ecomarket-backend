package pe.edu.upc.ecomarket.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Roles role);

    List<User> findAllByOrderByIdAsc();
}

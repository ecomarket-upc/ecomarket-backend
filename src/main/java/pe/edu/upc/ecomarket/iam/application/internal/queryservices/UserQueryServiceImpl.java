package pe.edu.upc.ecomarket.iam.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.ecomarket.iam.domain.services.UserQueryService;
import pe.edu.upc.ecomarket.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ForbiddenOperationException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAllByOrderByIdAsc();
    }

    @Override
    public User handle(GetUserByIdQuery query) {
        if (!query.requester().canManage(query.userId())) {
            throw new ForbiddenOperationException("Solo puedes consultar tu propio perfil");
        }
        return userRepository.findById(query.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", query.userId()));
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email().trim().toLowerCase());
    }
}

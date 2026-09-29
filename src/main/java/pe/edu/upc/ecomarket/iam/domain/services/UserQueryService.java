package pe.edu.upc.ecomarket.iam.domain.services;

import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.ecomarket.iam.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {

    List<User> handle(GetAllUsersQuery query);

    User handle(GetUserByIdQuery query);

    Optional<User> handle(GetUserByEmailQuery query);
}

package pe.edu.upc.ecomarket.iam.domain.services;

import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;
import pe.edu.upc.ecomarket.iam.domain.model.commands.DeactivateUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SeedAdminUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.AuthenticatedUser;

public interface UserCommandService {

    AuthenticatedUser handle(SignUpCommand command);

    AuthenticatedUser handle(SignInCommand command);

    User handle(UpdateUserCommand command);

    void handle(DeactivateUserCommand command);

    void handle(SeedAdminUserCommand command);
}

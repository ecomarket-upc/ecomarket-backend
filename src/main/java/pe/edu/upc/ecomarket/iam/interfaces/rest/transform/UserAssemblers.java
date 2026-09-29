package pe.edu.upc.ecomarket.iam.interfaces.rest.transform;

import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.SignInResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.SignUpResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.UpdateUserResource;
import pe.edu.upc.ecomarket.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

/**
 * Converts between REST resources and IAM commands or aggregates.
 */
public final class UserAssemblers {

    private UserAssemblers() {
    }

    public static SignUpCommand toCommand(SignUpResource resource) {
        return new SignUpCommand(resource.firstName(), resource.lastName(), resource.email(),
                resource.password(), resource.role());
    }

    public static SignInCommand toCommand(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }

    public static UpdateUserCommand toCommand(Long userId, UpdateUserResource resource, Requester requester) {
        return new UpdateUserCommand(userId, resource.firstName(), resource.lastName(), requester);
    }

    public static UserResource toResource(User user) {
        return new UserResource(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getRole(), user.isActive(), user.getCreatedAt());
    }

    public static AuthenticatedUserResource toResource(AuthenticatedUser authenticatedUser) {
        return new AuthenticatedUserResource(authenticatedUser.token(), "Bearer",
                authenticatedUser.expiresInSeconds(), toResource(authenticatedUser.user()));
    }
}

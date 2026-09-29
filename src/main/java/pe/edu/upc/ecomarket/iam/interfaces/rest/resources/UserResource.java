package pe.edu.upc.ecomarket.iam.interfaces.rest.resources;

import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;

import java.time.Instant;

/**
 * Public view of a user. It never includes the password.
 */
public record UserResource(
        Long id,
        String firstName,
        String lastName,
        String email,
        Roles role,
        boolean active,
        Instant createdAt
) {
}

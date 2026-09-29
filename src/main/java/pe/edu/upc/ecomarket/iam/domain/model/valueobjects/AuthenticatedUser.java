package pe.edu.upc.ecomarket.iam.domain.model.valueobjects;

import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;

/**
 * Result of a successful sign in or sign up: the user and the bearer token issued for them.
 */
public record AuthenticatedUser(User user, String token, long expiresInSeconds) {
}

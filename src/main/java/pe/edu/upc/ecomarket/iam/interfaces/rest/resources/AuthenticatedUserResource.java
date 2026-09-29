package pe.edu.upc.ecomarket.iam.interfaces.rest.resources;

/**
 * Sign up and sign in response. The client sends {@code token} in the
 * {@code Authorization: Bearer <token>} header of the following requests.
 */
public record AuthenticatedUserResource(
        String token,
        String tokenType,
        long expiresIn,
        UserResource user
) {
}

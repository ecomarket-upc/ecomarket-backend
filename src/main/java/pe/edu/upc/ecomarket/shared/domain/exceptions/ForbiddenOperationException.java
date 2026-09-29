package pe.edu.upc.ecomarket.shared.domain.exceptions;

/**
 * The authenticated user is not allowed to act on this resource, for example a seller
 * editing a store owned by someone else. Mapped to HTTP 403.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}

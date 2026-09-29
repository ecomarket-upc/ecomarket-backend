package pe.edu.upc.ecomarket.shared.domain.exceptions;

/**
 * The operation clashes with existing data, such as a duplicated email or name. Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

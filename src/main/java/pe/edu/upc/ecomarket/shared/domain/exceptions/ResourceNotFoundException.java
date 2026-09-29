package pe.edu.upc.ecomarket.shared.domain.exceptions;

/**
 * The requested resource does not exist. Mapped to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con id " + id + " no existe");
    }
}

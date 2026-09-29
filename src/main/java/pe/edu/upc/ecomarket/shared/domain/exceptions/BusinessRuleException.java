package pe.edu.upc.ecomarket.shared.domain.exceptions;

/**
 * The request is well formed but breaks a business rule. Mapped to HTTP 400.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

package pe.edu.upc.ecomarket.exceptions;

/**
 * La petición está bien formada pero no cumple una regla del negocio. Responde 400 Bad Request.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

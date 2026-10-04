package pe.edu.upc.ecomarket.exceptions;

/**
 * El usuario no puede actuar sobre este recurso, por ejemplo un comerciante que edita
 * el comercio de otro. Responde 403 Forbidden.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}

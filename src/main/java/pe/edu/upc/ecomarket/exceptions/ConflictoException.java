package pe.edu.upc.ecomarket.exceptions;

/**
 * El dato choca con uno existente, por ejemplo un correo o nombre repetido. Responde 409 Conflict.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}

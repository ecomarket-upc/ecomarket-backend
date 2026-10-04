package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del registro y del login: el token JWT y los datos del usuario (TS01).
 * El cliente envía el token en el header {@code Authorization: Bearer <token>}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRespuestaDTO {

    private String token;
    private String tipo;
    private UsuarioDTO usuario;
}

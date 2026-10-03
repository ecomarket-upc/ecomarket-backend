package pe.edu.upc.ecomarket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.models.Rol;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.UsuarioRepository;

import java.util.Optional;

/**
 * Obtiene el usuario que envió el token JWT en la petición actual.
 */
@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    /**
     * @return el usuario autenticado, o vacío si la petición no tiene token
     */
    public Optional<Usuario> buscar() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UserDetails detalles)) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreo(detalles.getUsername());
    }

    /**
     * @throws AuthenticationCredentialsNotFoundException (401) si no hay sesión
     */
    public Usuario obtener() {
        return buscar().orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Debes iniciar sesión"));
    }

    public static boolean esAdministrador(Usuario usuario) {
        return usuario != null && Rol.ADMINISTRADOR.equals(usuario.getRol().getNombre());
    }
}

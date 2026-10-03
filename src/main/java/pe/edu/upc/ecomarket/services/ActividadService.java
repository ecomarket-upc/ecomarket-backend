package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.models.Busqueda;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.models.Visita;
import pe.edu.upc.ecomarket.repository.BusquedaRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

/**
 * Registra las visitas y las búsquedas que alimentan las estadísticas y las recomendaciones (TS13).
 */
@Service
@RequiredArgsConstructor
public class ActividadService {

    private final VisitaRepository visitaRepository;
    private final BusquedaRepository busquedaRepository;
    private final UsuarioActual usuarioActual;

    /**
     * TS13 escenario 1: registra la visita con su fecha y, si existe, la búsqueda de origen.
     * No cuenta las consultas del propio dueño ni del administrador.
     *
     * @param producto nulo si la visita fue al detalle del comercio
     */
    @Transactional
    public void registrarVisita(Comercio comercio, Producto producto, String terminoBusqueda) {
        Usuario usuario = usuarioActual.buscar().orElse(null);
        if (usuario != null && (UsuarioActual.esAdministrador(usuario)
                || usuario.getId().equals(comercio.getPropietario().getId()))) {
            return;
        }
        Visita visita = new Visita();
        visita.setComercio(comercio);
        visita.setProducto(producto);
        visita.setUsuario(usuario);
        visita.setTerminoBusqueda(limpiar(terminoBusqueda));
        visitaRepository.save(visita);
    }

    /** Guarda el término buscado (US17, TS13). */
    @Transactional
    public void registrarBusqueda(String termino, String tipo) {
        String limpio = limpiar(termino);
        if (limpio == null) {
            return;
        }
        Busqueda busqueda = new Busqueda();
        busqueda.setUsuario(usuarioActual.buscar().orElse(null));
        busqueda.setTermino(limpio);
        busqueda.setTipo(tipo);
        busquedaRepository.save(busqueda);
    }

    private static String limpiar(String termino) {
        if (termino == null || termino.isBlank()) {
            return null;
        }
        String limpio = termino.trim().toLowerCase();
        return limpio.length() > 100 ? limpio.substring(0, 100) : limpio;
    }
}

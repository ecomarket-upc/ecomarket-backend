package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ComercioDTO;
import pe.edu.upc.ecomarket.dto.ComercioRespuestaDTO;
import pe.edu.upc.ecomarket.dto.CoordenadasDTO;
import pe.edu.upc.ecomarket.dto.PromocionRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ValidacionComercioDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Promocion;
import pe.edu.upc.ecomarket.models.Suscripcion;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.ComercioRepository;
import pe.edu.upc.ecomarket.repository.FavoritoRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.repository.PromocionRepository;
import pe.edu.upc.ecomarket.repository.SuscripcionRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.Comparator;
import java.util.List;

/**
 * Registro, edición, validación y búsqueda geolocalizada de comercios
 * (US05, US07, US08, US19, US21, US30, TS02, TS03, TS10).
 */
@Service
@RequiredArgsConstructor
public class ComercioService {

    /** Radio máximo de la búsqueda por cercanía (TS03). */
    public static final double RADIO_MAXIMO_KM = 50;
    private static final double RADIO_TIERRA_KM = 6371;
    private static final double KM_POR_GRADO = 111.32;

    private final ComercioRepository comercioRepository;
    private final ProductoRepository productoRepository;
    private final FavoritoRepository favoritoRepository;
    private final VisitaRepository visitaRepository;
    private final PromocionRepository promocionRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final GeocodificacionService geocodificacionService;
    private final UsuarioActual usuarioActual;

    @Transactional
    public ComercioRespuestaDTO crear(ComercioDTO dto) {
        Comercio comercio = new Comercio();
        comercio.setPropietario(usuarioActual.obtener());
        copiarDatos(dto, comercio);
        return aDTO(comercioRepository.save(comercio));
    }

    /** Lista pública: solo comercios aprobados (US19, escenario 3). */
    public List<ComercioRespuestaDTO> listarAprobados(String distrito) {
        List<Comercio> comercios = (distrito == null || distrito.isBlank())
                ? comercioRepository.findByEstadoOrderByIdAsc(EstadoComercio.APPROVED)
                : comercioRepository.findByEstadoAndDistritoIgnoreCaseOrderByIdAsc(EstadoComercio.APPROVED, distrito.trim());
        return comercios.stream().map(this::aDTO).toList();
    }

    /**
     * Un comercio no aprobado solo lo ven su dueño y el administrador; para los demás "no existe" (US08).
     */
    public Comercio obtenerVisible(Long id) {
        Comercio comercio = buscar(id);
        if (comercio.getEstado() != EstadoComercio.APPROVED && !puedeAdministrar(comercio)) {
            throw new RecursoNoEncontradoException("Comercio", id);
        }
        return comercio;
    }

    /** US06, escenario 3. */
    public List<ComercioRespuestaDTO> buscarPorTexto(String texto) {
        return comercioRepository.findByEstadoAndNombreContainingIgnoreCaseOrderByNombreAsc(EstadoComercio.APPROVED,
                texto.trim()).stream().map(this::aDTO).toList();
    }

    public List<ComercioRespuestaDTO> misComercios() {
        return comercioRepository.findByPropietarioIdOrderByIdAsc(usuarioActual.obtener().getId()).stream()
                .map(this::aDTO).toList();
    }

    public List<ComercioRespuestaDTO> pendientes() {
        return comercioRepository.findByEstadoOrderByIdAsc(EstadoComercio.PENDING).stream().map(this::aDTO).toList();
    }

    /**
     * Si el comercio había sido rechazado, al corregirlo vuelve a quedar pendiente (US21, escenario 2).
     */
    @Transactional
    public ComercioRespuestaDTO actualizar(Long id, ComercioDTO dto) {
        Comercio comercio = buscarPropio(id, "Solo puedes editar tus propios comercios");
        copiarDatos(dto, comercio);
        if (comercio.getEstado() == EstadoComercio.REJECTED) {
            comercio.setEstado(EstadoComercio.PENDING);
            comercio.setMotivoRechazo(null);
        }
        return aDTO(comercioRepository.save(comercio));
    }

    /**
     * Borra el comercio junto con sus productos y registros asociados (TS02).
     */
    @Transactional
    public void eliminar(Long id) {
        Comercio comercio = buscarPropio(id, "Solo puedes eliminar tus propios comercios");
        visitaRepository.deleteByComercioId(id);
        favoritoRepository.deleteByComercioId(id);
        promocionRepository.deleteByComercioId(id);
        suscripcionRepository.deleteByComercioId(id);
        productoRepository.deleteAll(productoRepository.findByComercioId(id));
        comercioRepository.delete(comercio);
    }

    /** Aprobación o rechazo por el administrador; rechazar exige un motivo (US30). */
    @Transactional
    public ComercioRespuestaDTO validar(Long id, ValidacionComercioDTO dto) {
        Comercio comercio = buscar(id);
        if (dto.getEstado() == EstadoComercio.PENDING) {
            throw new ReglaNegocioException("El estado debe ser APPROVED o REJECTED");
        }
        if (dto.getEstado() == EstadoComercio.REJECTED && (dto.getMotivo() == null || dto.getMotivo().isBlank())) {
            throw new ReglaNegocioException("Debes indicar el motivo del rechazo");
        }
        comercio.setEstado(dto.getEstado());
        comercio.setMotivoRechazo(dto.getEstado() == EstadoComercio.REJECTED ? dto.getMotivo().trim() : null);
        return aDTO(comercioRepository.save(comercio));
    }

    /**
     * Comercios aprobados dentro del radio, del más cercano al más lejano (US05, US07, TS03).
     */
    public List<ComercioCercanoDTO> cercanos(double latitud, double longitud, double radioKm) {
        validarUbicacion(latitud, longitud, radioKm);
        double deltaLatitud = radioKm / KM_POR_GRADO;
        double deltaLongitud = radioKm / (KM_POR_GRADO * Math.max(Math.cos(Math.toRadians(latitud)), 0.01));
        return comercioRepository.findByEstadoAndLatitudBetweenAndLongitudBetween(EstadoComercio.APPROVED,
                        latitud - deltaLatitud, latitud + deltaLatitud, longitud - deltaLongitud, longitud + deltaLongitud)
                .stream()
                .map(c -> new ComercioCercanoDTO(aDTO(c), redondear(distanciaKm(latitud, longitud, c.getLatitud(), c.getLongitud()))))
                .filter(c -> c.getDistanciaKm() <= radioKm)
                .sorted(Comparator.comparingDouble(ComercioCercanoDTO::getDistanciaKm))
                .toList();
    }

    public void validarUbicacion(double latitud, double longitud, double radioKm) {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new ReglaNegocioException("La latitud debe estar entre -90 y 90 y la longitud entre -180 y 180");
        }
        if (radioKm <= 0 || radioKm > RADIO_MAXIMO_KM) {
            throw new ReglaNegocioException("El radio debe ser mayor que 0 y como máximo 50 km");
        }
    }

    /** Distancia entre dos puntos en km con la fórmula de Haversine. */
    public static double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(a));
    }

    public static double redondear(double km) {
        return Math.round(km * 100) / 100.0;
    }

    /** Tiene un plan premium activo (US25, TS14). */
    public boolean esPremium(Long comercioId) {
        return suscripcionRepository.findByComercioIdOrderByFechaFinDesc(comercioId).stream()
                .anyMatch(Suscripcion::estaActiva);
    }

    /** Solo las promociones vigentes; las vencidas ya no se muestran (US26, escenario 3). */
    public List<PromocionRespuestaDTO> promocionesVigentes(Long comercioId) {
        return promocionRepository.findByComercioIdOrderByFechaInicioAsc(comercioId).stream()
                .filter(Promocion::estaVigente)
                .map(p -> new PromocionRespuestaDTO(p.getId(), comercioId, p.getTitulo(), p.getDescripcion(),
                        p.getFechaInicio(), p.getFechaFin()))
                .toList();
    }

    public Comercio buscar(Long id) {
        return comercioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Comercio", id));
    }

    /** El dueño o el administrador pueden administrar el comercio. */
    public boolean puedeAdministrar(Comercio comercio) {
        return usuarioActual.buscar()
                .map(u -> UsuarioActual.esAdministrador(u) || u.getId().equals(comercio.getPropietario().getId()))
                .orElse(false);
    }

    public Comercio buscarPropio(Long id, String mensajeSiNoEsDueno) {
        Comercio comercio = buscar(id);
        Usuario usuario = usuarioActual.obtener();
        if (!UsuarioActual.esAdministrador(usuario) && !usuario.getId().equals(comercio.getPropietario().getId())) {
            throw new AccesoDenegadoException(mensajeSiNoEsDueno);
        }
        return comercio;
    }

    /**
     * Usa las coordenadas enviadas; si no vienen, las obtiene de la dirección (TS10).
     */
    private void copiarDatos(ComercioDTO dto, Comercio comercio) {
        if ((dto.getLatitud() == null) != (dto.getLongitud() == null)) {
            throw new ReglaNegocioException("Envía la latitud y la longitud juntas, o ninguna de las dos");
        }
        comercio.setNombre(dto.getNombre().trim());
        comercio.setDescripcion(dto.getDescripcion());
        comercio.setTelefono(dto.getTelefono());
        comercio.setCorreoContacto(dto.getCorreoContacto());
        comercio.setHorario(dto.getHorario());
        comercio.setDireccion(dto.getDireccion().trim());
        comercio.setDistrito(dto.getDistrito().trim());
        comercio.setCiudad(dto.getCiudad().trim());
        if (dto.getLatitud() != null) {
            comercio.setLatitud(dto.getLatitud());
            comercio.setLongitud(dto.getLongitud());
            return;
        }
        CoordenadasDTO coordenadas = geocodificacionService.geocodificar(dto.getDireccion(), dto.getDistrito(), dto.getCiudad())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se pudo ubicar la dirección. Indica la latitud y la longitud del comercio en el mapa"));
        comercio.setLatitud(coordenadas.getLatitud());
        comercio.setLongitud(coordenadas.getLongitud());
    }

    public ComercioRespuestaDTO aDTO(Comercio comercio) {
        ComercioRespuestaDTO dto = new ComercioRespuestaDTO();
        dto.setId(comercio.getId());
        dto.setNombre(comercio.getNombre());
        dto.setDescripcion(comercio.getDescripcion());
        dto.setTelefono(comercio.getTelefono());
        dto.setCorreoContacto(comercio.getCorreoContacto());
        dto.setHorario(comercio.getHorario());
        dto.setDireccion(comercio.getDireccion());
        dto.setDistrito(comercio.getDistrito());
        dto.setCiudad(comercio.getCiudad());
        dto.setLatitud(comercio.getLatitud());
        dto.setLongitud(comercio.getLongitud());
        dto.setEstado(comercio.getEstado().name());
        dto.setMotivoRechazo(comercio.getMotivoRechazo());
        dto.setPropietarioId(comercio.getPropietario().getId());
        dto.setPropietarioNombre(comercio.getPropietario().getNombres() + " " + comercio.getPropietario().getApellidos());
        dto.setFechaRegistro(comercio.getFechaRegistro());
        dto.setPremium(esPremium(comercio.getId()));
        dto.setPromociones(promocionesVigentes(comercio.getId()));
        return dto;
    }
}

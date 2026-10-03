package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ConteoDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.dto.RecomendacionComerciosDTO;
import pe.edu.upc.ecomarket.dto.RecomendacionProductosDTO;
import pe.edu.upc.ecomarket.models.Busqueda;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.BusquedaRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recomendaciones sencillas, sin otra IA (US17, US18, TS12). Usan la información que ya existe:
 * preferencias + búsquedas + Eco-Etiquetas + ubicación.
 */
@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private static final int MAXIMO = 10;
    private static final String POR_PREFERENCIAS = "preferencias";

    private final PreferenciaService preferenciaService;
    private final ProductoService productoService;
    private final ComercioService comercioService;
    private final BusquedaRepository busquedaRepository;
    private final ProductoRepository productoRepository;
    private final VisitaRepository visitaRepository;
    private final UsuarioActual usuarioActual;

    /**
     * US17: puntaje = 2 por cada Eco-Etiqueta preferida + 1 si el producto coincide con una búsqueda reciente.
     * Si el consumidor no tiene preferencias ni búsquedas, devuelve los productos sostenibles más consultados.
     */
    public RecomendacionProductosDTO productos() {
        Usuario usuario = usuarioActual.obtener();
        Set<Long> preferidas = idsPreferidas(usuario);
        List<String> terminos = busquedaRepository.findTop20ByUsuarioIdOrderByFechaDesc(usuario.getId()).stream()
                .map(Busqueda::getTermino).distinct().toList();

        if (!preferidas.isEmpty() || !terminos.isEmpty()) {
            List<ProductoRespuestaDTO> recomendados = productoService.buscar("", "", "").stream()
                    .map(p -> Map.entry(p, puntaje(p, preferidas, terminos)))
                    .filter(e -> e.getValue() > 0)
                    .sorted(Map.Entry.<Producto, Integer>comparingByValue().reversed())
                    .limit(MAXIMO)
                    .map(e -> productoService.aDTO(e.getKey()))
                    .toList();
            if (!recomendados.isEmpty()) {
                return new RecomendacionProductosDTO(POR_PREFERENCIAS, recomendados);
            }
        }
        return new RecomendacionProductosDTO("mas-consultados", masConsultados());
    }

    /**
     * US18: comercios cercanos con más productos afines a las preferencias. Si ninguno coincide,
     * los comercios aprobados más cercanos.
     */
    public RecomendacionComerciosDTO comercios(double latitud, double longitud, double radioKm) {
        Set<Long> preferidas = idsPreferidas(usuarioActual.obtener());
        List<ComercioCercanoDTO> cercanos = comercioService.cercanos(latitud, longitud, radioKm);
        if (!preferidas.isEmpty()) {
            List<ComercioCercanoDTO> afines = cercanos.stream()
                    .map(c -> Map.entry(c, productosAfines(c.getComercio().getId(), preferidas)))
                    .filter(e -> e.getValue() > 0)
                    .sorted(Map.Entry.<ComercioCercanoDTO, Long>comparingByValue().reversed()
                            .thenComparing(e -> e.getKey().getDistanciaKm()))
                    .limit(MAXIMO)
                    .map(Map.Entry::getKey)
                    .toList();
            if (!afines.isEmpty()) {
                return new RecomendacionComerciosDTO(POR_PREFERENCIAS, afines);
            }
        }
        return new RecomendacionComerciosDTO("mas-cercanos", cercanos.stream().limit(MAXIMO).toList());
    }

    private Set<Long> idsPreferidas(Usuario usuario) {
        return preferenciaService.ecoEtiquetasDe(usuario.getId()).stream()
                .map(EcoEtiqueta::getId).collect(Collectors.toSet());
    }

    private static int puntaje(Producto producto, Set<Long> preferidas, List<String> terminos) {
        int puntos = 2 * (int) producto.getEcoEtiquetas().stream().filter(e -> preferidas.contains(e.getId())).count();
        String texto = (producto.getNombre() + " " + Objects.requireNonNullElse(producto.getDescripcion(), "")).toLowerCase();
        if (terminos.stream().anyMatch(texto::contains)) {
            puntos += 1;
        }
        return puntos;
    }

    private long productosAfines(Long comercioId, Set<Long> preferidas) {
        return productoRepository.findByComercioIdAndActivoTrueOrderByNombreAsc(comercioId).stream()
                .filter(p -> p.getEcoEtiquetas().stream().anyMatch(e -> preferidas.contains(e.getId())))
                .count();
    }

    /** Productos con Eco-Etiquetas más visitados; si aún no hay visitas, cualquier producto sostenible. */
    private List<ProductoRespuestaDTO> masConsultados() {
        List<ProductoRespuestaDTO> visitados = visitaRepository.productosMasConsultados().stream()
                .map(ConteoDTO::getId)
                .map(productoRepository::findById)
                .flatMap(Optional::stream)
                .filter(p -> !p.getEcoEtiquetas().isEmpty())
                .limit(MAXIMO)
                .map(productoService::aDTO)
                .toList();
        if (!visitados.isEmpty()) {
            return visitados;
        }
        return productoService.buscar("", "", "").stream()
                .filter(p -> !p.getEcoEtiquetas().isEmpty())
                .sorted(Comparator.comparing(Producto::getFechaRegistro).reversed())
                .limit(MAXIMO)
                .map(productoService::aDTO)
                .toList();
    }
}

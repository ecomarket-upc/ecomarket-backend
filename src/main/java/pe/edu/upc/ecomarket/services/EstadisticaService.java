package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.dto.ConteoDTO;
import pe.edu.upc.ecomarket.dto.EstadisticasAvanzadasDTO;
import pe.edu.upc.ecomarket.dto.EstadisticasDTO;
import pe.edu.upc.ecomarket.dto.ResumenActividadDTO;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Categoria;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.models.Visita;
import pe.edu.upc.ecomarket.repository.FavoritoRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Estadísticas del comerciante (US22, US23, US27, TS13) y resumen de actividad del consumidor (US31).
 */
@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private static final int MESES_EVOLUCION = 6;

    private final ComercioService comercioService;
    private final VisitaRepository visitaRepository;
    private final ProductoRepository productoRepository;
    private final FavoritoRepository favoritoRepository;
    private final UsuarioActual usuarioActual;

    /**
     * Solo el dueño (o el administrador) consulta las estadísticas; otro comerciante recibe 403 (TS13).
     *
     * @param periodo "semana", "mes" o "anio"
     */
    public EstadisticasDTO delComercio(Long comercioId, String periodo) {
        Comercio comercio = comercioService.buscarPropio(comercioId, "Solo puedes ver las estadísticas de tus propios comercios");
        LocalDate hasta = LocalDate.now();
        LocalDate desde = switch (periodo) {
            case "semana" -> hasta.minusDays(6);
            case "mes" -> hasta.minusDays(29);
            case "anio" -> hasta.minusDays(364);
            default -> throw new ReglaNegocioException("El periodo debe ser semana, mes o anio");
        };
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        EstadisticasDTO dto = new EstadisticasDTO();
        dto.setComercioId(comercio.getId());
        dto.setPeriodo(periodo);
        dto.setDesde(desde);
        dto.setHasta(hasta);
        dto.setVisitasComercio(visitaRepository.countByComercioIdAndProductoIsNullAndFechaBetween(comercio.getId(), inicio, fin));
        dto.setVisitasProductos(visitaRepository.contarPorProducto(comercio.getId(), inicio, fin));
        dto.setTerminosBusqueda(visitaRepository.contarTerminos(comercio.getId(), inicio, fin).stream().limit(10).toList());
        if (dto.getVisitasComercio() == 0 && dto.getVisitasProductos().isEmpty()) {
            dto.setMensajeVisitas("Aún no hay visitas en este periodo");
        }
        if (dto.getTerminosBusqueda().isEmpty()) {
            dto.setMensajeBusquedas("Aún no hay búsquedas registradas que lleven a tu comercio");
        }
        dto.setPremium(comercioService.esPremium(comercio.getId()));
        if (dto.isPremium()) {
            dto.setAvanzadas(avanzadas(comercio, inicio, fin));
        } else {
            dto.setMensajePremium("Contrata el plan premium para ver la comparación con tu categoría y la evolución mensual");
        }
        return dto;
    }

    /**
     * US27: compara las visitas por producto del comercio con el promedio de su categoría principal
     * (la que tiene más productos) y muestra las visitas de los últimos 6 meses.
     */
    private EstadisticasAvanzadasDTO avanzadas(Comercio comercio, LocalDateTime inicio, LocalDateTime fin) {
        List<Producto> productos = productoRepository.findByComercioIdAndActivoTrueOrderByNombreAsc(comercio.getId());
        Categoria principal = productos.stream()
                .collect(Collectors.groupingBy(Producto::getCategoria, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        long visitasPropias = visitaRepository.contarPorProducto(comercio.getId(), inicio, fin).stream()
                .mapToLong(ConteoDTO::getCantidad).sum();
        double promedioPropio = productos.isEmpty() ? 0 : (double) visitasPropias / productos.size();
        double promedioCategoria = 0;
        if (principal != null) {
            long productosCategoria = productoRepository.countByCategoriaIdAndActivoTrueAndComercioEstado(
                    principal.getId(), EstadoComercio.APPROVED);
            long visitasCategoria = visitaRepository.contarPorCategoria(principal.getId(), inicio, fin);
            promedioCategoria = productosCategoria == 0 ? 0 : (double) visitasCategoria / productosCategoria;
        }

        LocalDateTime desdeEvolucion = YearMonth.now().minusMonths(MESES_EVOLUCION - 1).atDay(1).atStartOfDay();
        Map<YearMonth, Long> porMes = new TreeMap<>();
        for (int i = MESES_EVOLUCION - 1; i >= 0; i--) {
            porMes.put(YearMonth.now().minusMonths(i), 0L);
        }
        for (Visita visita : visitaRepository.findByComercioIdAndFechaAfter(comercio.getId(), desdeEvolucion)) {
            porMes.merge(YearMonth.from(visita.getFecha()), 1L, Long::sum);
        }
        List<ConteoDTO> evolucion = new ArrayList<>();
        porMes.forEach((mes, cantidad) -> evolucion.add(new ConteoDTO(null, mes.toString(), cantidad)));

        return new EstadisticasAvanzadasDTO(principal == null ? null : principal.getNombre(),
                redondear(promedioPropio), redondear(promedioCategoria), evolucion);
    }

    /**
     * US31: cuántos productos consultó el consumidor por Eco-Etiqueta y cuántos comercios guardó como favoritos.
     */
    public ResumenActividadDTO resumenConsumidor() {
        Usuario usuario = usuarioActual.obtener();
        List<Producto> consultados = visitaRepository.findByUsuarioIdAndProductoIsNotNull(usuario.getId()).stream()
                .map(Visita::getProducto)
                .collect(Collectors.toMap(Producto::getId, Function.identity(), (a, b) -> a))
                .values().stream().toList();
        Map<String, Long> porEtiqueta = new TreeMap<>();
        for (Producto producto : consultados) {
            for (EcoEtiqueta etiqueta : producto.getEcoEtiquetas()) {
                porEtiqueta.merge(etiqueta.getNombre(), 1L, Long::sum);
            }
        }
        List<ConteoDTO> conteo = porEtiqueta.entrySet().stream()
                .map(e -> new ConteoDTO(null, e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ConteoDTO::getCantidad).reversed())
                .toList();
        long favoritos = favoritoRepository.countByUsuarioId(usuario.getId());
        String mensaje = consultados.isEmpty() && favoritos == 0
                ? "Aún no tienes actividad. Explora los comercios sostenibles cercanos a ti"
                : null;
        return new ResumenActividadDTO(conteo, favoritos, mensaje);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }
}

package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.ClasificacionDTO;
import pe.edu.upc.ecomarket.dto.ProductoComparadoDTO;
import pe.edu.upc.ecomarket.dto.ProductoDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.EcoEtiquetaRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Productos de los comercios: publicación, edición, eliminación, catálogo, búsqueda y comparación
 * (US06, US07, US10, US11, US12, US15, US16, US20, US29, TS04).
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    public static final String MOTOR_MANUAL = "manual";

    private final ProductoRepository productoRepository;
    private final ComercioService comercioService;
    private final CategoriaService categoriaService;
    private final EcoEtiquetaService ecoEtiquetaService;
    private final UsuarioActual usuarioActual;
    private final ClasificadorService clasificadorService;
    private final EcoEtiquetaRepository ecoEtiquetaRepository;
    private final PreferenciaService preferenciaService;

    /**
     * US20: el comerciante publica en uno de sus comercios. Si no eligió Eco-Etiquetas, el sistema
     * las asigna automáticamente con IA (US13, TS06 escenario 1).
     */
    @Transactional
    public ProductoRespuestaDTO crear(ProductoDTO dto) {
        Comercio comercio = comercioService.buscar(dto.getComercioId());
        if (!comercio.getPropietario().getId().equals(usuarioActual.obtener().getId())) {
            throw new AccesoDenegadoException("Solo puedes publicar productos en tus propios comercios");
        }
        Producto producto = new Producto();
        producto.setComercio(comercio);
        copiarDatos(dto, producto);
        if (dto.getEcoEtiquetaIds() == null) {
            aplicarClasificacion(producto);
        }
        return aDTO(productoRepository.save(producto));
    }

    /** TS06 escenario 2: el dueño vuelve a clasificar su producto. */
    @Transactional
    public ClasificacionDTO clasificar(Long id) {
        Producto producto = buscarPropio(id, "Solo puedes clasificar tus propios productos");
        String motor = aplicarClasificacion(producto);
        productoRepository.save(producto);
        String mensaje = producto.getEcoEtiquetas().isEmpty()
                ? "La descripción no menciona características sostenibles: no se asignaron Eco-Etiquetas"
                : "Se asignaron " + producto.getEcoEtiquetas().size() + " Eco-Etiquetas";
        return new ClasificacionDTO(aDTO(producto), motor, mensaje);
    }

    /** Solo se ofrecen las Eco-Etiquetas activas (US28, escenario 3). */
    private String aplicarClasificacion(Producto producto) {
        String texto = producto.getNombre() + ". " + (producto.getDescripcion() == null ? "" : producto.getDescripcion());
        ClasificadorService.Resultado resultado = clasificadorService.clasificar(texto,
                ecoEtiquetaRepository.findByActivaTrueOrderByNombreAsc());
        producto.setEcoEtiquetas(new HashSet<>(resultado.ecoEtiquetas()));
        producto.setMotorClasificacion(resultado.motor());
        return resultado.motor();
    }

    /** US11: un producto eliminado o de un comercio no aprobado "no existe" para el público. */
    public Producto obtenerVisible(Long id) {
        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        Comercio comercio = producto.getComercio();
        if (comercio.getEstado() != EstadoComercio.APPROVED && !comercioService.puedeAdministrar(comercio)) {
            throw new RecursoNoEncontradoException("Producto", id);
        }
        return producto;
    }

    /** US20 y US16: si se envían Eco-Etiquetas, quedan como asignadas manualmente. */
    @Transactional
    public ProductoRespuestaDTO actualizar(Long id, ProductoDTO dto) {
        Producto producto = buscarPropio(id, "Solo puedes editar tus propios productos");
        if (!producto.getComercio().getId().equals(dto.getComercioId())) {
            throw new ReglaNegocioException("El comercio de un producto no se puede cambiar");
        }
        copiarDatos(dto, producto);
        return aDTO(productoRepository.save(producto));
    }

    /** US20 y US29: el dueño o el administrador retiran el producto (eliminación lógica). */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPropio(id, "Solo puedes eliminar tus propios productos");
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    /** US10: catálogo de un comercio aprobado. */
    public List<ProductoRespuestaDTO> catalogo(Long comercioId) {
        Comercio comercio = comercioService.obtenerVisible(comercioId);
        return productoRepository.findByComercioIdAndActivoTrueOrderByNombreAsc(comercio.getId()).stream()
                .map(this::aDTO).toList();
    }

    public List<ProductoRespuestaDTO> misProductos() {
        return productoRepository.findByComercioPropietarioIdAndActivoTrueOrderByIdAsc(usuarioActual.obtener().getId())
                .stream().map(this::aDTO).toList();
    }

    /** US06, US07, US15, TS04: todos los filtros son opcionales. */
    public List<Producto> buscar(String nombre, String categoria, String ecoEtiqueta) {
        return productoRepository.buscar(texto(nombre), texto(categoria), texto(ecoEtiqueta));
    }

    /**
     * Resultados ordenados: primero los destacados de comercios premium (US25) y luego los que coinciden
     * con más preferencias del consumidor autenticado (US04). Sin preferencias, el orden no se personaliza.
     */
    public List<ProductoRespuestaDTO> buscarOrdenado(String nombre, String categoria, String ecoEtiqueta) {
        Set<EcoEtiqueta> preferidas = usuarioActual.buscar()
                .map(Usuario::getId)
                .map(preferenciaService::ecoEtiquetasDe)
                .orElse(Set.of());
        return buscar(nombre, categoria, ecoEtiqueta).stream()
                .map(this::aDTO)
                .sorted(Comparator.comparing(ProductoRespuestaDTO::isDestacado).reversed()
                        .thenComparing(Comparator.comparingLong((ProductoRespuestaDTO p) -> coincidencias(p, preferidas)).reversed()))
                .toList();
    }

    /** Cuántas Eco-Etiquetas del producto están entre las preferidas. */
    public static long coincidencias(ProductoRespuestaDTO producto, Set<EcoEtiqueta> preferidas) {
        Set<Long> ids = preferidas.stream().map(EcoEtiqueta::getId).collect(Collectors.toSet());
        return producto.getEcoEtiquetas().stream().filter(e -> ids.contains(e.getId())).count();
    }

    /**
     * US12: compara de 2 a 4 productos. Si llega la ubicación, agrega la distancia a cada comercio.
     */
    public List<ProductoComparadoDTO> comparar(List<Long> ids, Double latitud, Double longitud) {
        Set<Long> distintos = new HashSet<>(ids);
        if (distintos.size() < 2 || distintos.size() > 4) {
            throw new ReglaNegocioException("Debes elegir entre 2 y 4 productos distintos para compararlos");
        }
        if ((latitud == null) != (longitud == null)) {
            throw new ReglaNegocioException("Envía la latitud y la longitud juntas, o ninguna de las dos");
        }
        return distintos.stream()
                .map(this::obtenerVisible)
                .sorted(Comparator.comparing(Producto::getPrecio))
                .map(p -> new ProductoComparadoDTO(aDTO(p), latitud == null ? null
                        : ComercioService.redondear(ComercioService.distanciaKm(latitud, longitud,
                        p.getComercio().getLatitud(), p.getComercio().getLongitud()))))
                .toList();
    }

    public Producto buscarPropio(Long id, String mensajeSiNoEsDueno) {
        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        comercioService.buscarPropio(producto.getComercio().getId(), mensajeSiNoEsDueno);
        return producto;
    }

    private void copiarDatos(ProductoDTO dto, Producto producto) {
        producto.setCategoria(categoriaService.buscar(dto.getCategoriaId()));
        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setImagenUrl(dto.getImagenUrl());
        if (dto.getEcoEtiquetaIds() != null) {
            producto.setEcoEtiquetas(ecoEtiquetasActivas(dto.getEcoEtiquetaIds()));
            producto.setMotorClasificacion(MOTOR_MANUAL);
        }
    }

    private Set<EcoEtiqueta> ecoEtiquetasActivas(Set<Long> ids) {
        Set<EcoEtiqueta> etiquetas = new HashSet<>();
        for (Long id : ids) {
            EcoEtiqueta etiqueta = ecoEtiquetaService.buscar(id);
            if (!etiqueta.isActiva()) {
                throw new ReglaNegocioException("La Eco-Etiqueta " + etiqueta.getNombre() + " está desactivada");
            }
            etiquetas.add(etiqueta);
        }
        return etiquetas;
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public ProductoRespuestaDTO aDTO(Producto producto) {
        ProductoRespuestaDTO dto = new ProductoRespuestaDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setComercioId(producto.getComercio().getId());
        dto.setComercioNombre(producto.getComercio().getNombre());
        dto.setCategoriaId(producto.getCategoria().getId());
        dto.setCategoriaNombre(producto.getCategoria().getNombre());
        dto.setEcoEtiquetas(producto.getEcoEtiquetas().stream()
                .sorted(Comparator.comparing(EcoEtiqueta::getNombre))
                .map(ecoEtiquetaService::aDTO).toList());
        dto.setMotorClasificacion(producto.getMotorClasificacion());
        dto.setDestacado(comercioService.esPremium(producto.getComercio().getId()));
        if (producto.getEcoEtiquetas().isEmpty()) {
            dto.setAvisoEcoEtiquetas("El producto aún no tiene Eco-Etiquetas");
        }
        return dto;
    }
}

package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.FavoritoDTO;
import pe.edu.upc.ecomarket.dto.FavoritoRespuestaDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.ConflictoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Favorito;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.FavoritoRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.List;

/**
 * Comercios favoritos del consumidor autenticado (US09, TS11).
 */
@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ComercioService comercioService;
    private final UsuarioActual usuarioActual;

    /** Solo comercios aprobados; marcar dos veces el mismo comercio responde 409 (TS11). */
    @Transactional
    public FavoritoRespuestaDTO agregar(FavoritoDTO dto) {
        Usuario usuario = usuarioActual.obtener();
        Comercio comercio = comercioService.buscar(dto.getComercioId());
        if (comercio.getEstado() != EstadoComercio.APPROVED) {
            throw new RecursoNoEncontradoException("Comercio", dto.getComercioId());
        }
        if (favoritoRepository.existsByUsuarioIdAndComercioId(usuario.getId(), comercio.getId())) {
            throw new ConflictoException("El comercio ya está en tus favoritos");
        }
        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setComercio(comercio);
        return aDTO(favoritoRepository.save(favorito));
    }

    public List<FavoritoRespuestaDTO> listar() {
        return favoritoRepository.findByUsuarioIdOrderByFechaRegistroDesc(usuarioActual.obtener().getId()).stream()
                .map(this::aDTO).toList();
    }

    @Transactional
    public void eliminar(Long id) {
        Favorito favorito = favoritoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Favorito", id));
        if (!favorito.getUsuario().getId().equals(usuarioActual.obtener().getId())) {
            throw new AccesoDenegadoException("Solo puedes quitar tus propios favoritos");
        }
        favoritoRepository.delete(favorito);
    }

    private FavoritoRespuestaDTO aDTO(Favorito favorito) {
        return new FavoritoRespuestaDTO(favorito.getId(), comercioService.aDTO(favorito.getComercio()),
                favorito.getFechaRegistro());
    }
}

package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.PaqueteLaminas;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface PaqueteLaminasServiceAPI extends GenericServiceAPI<PaqueteLaminas, Long> {
    List<PaqueteLaminas> findByIdUsuario(Long idUsuario);
    List<PaqueteLaminas> findByIdUsuarioAndAbierto(Long idUsuario, byte abierto);
}
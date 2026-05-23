package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.AgendaUsuario;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface AgendaUsuarioServiceAPI extends GenericServiceAPI<AgendaUsuario, Long> {
    List<AgendaUsuario> findByIdUsuario(Long idUsuario);
    List<AgendaUsuario> findByIdPartido(Long idPartido);
    boolean existeDuplicado(Long idUsuario, Long idPartido);
}
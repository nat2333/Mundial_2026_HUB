package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.AgendaUsuario;

@Repository
public interface AgendaUsuarioRepository extends CrudRepository<AgendaUsuario, Long> {
    List<AgendaUsuario> findByIdUsuario(Long idUsuario);
    List<AgendaUsuario> findByIdPartido(Long idPartido);
    List<AgendaUsuario> findByIdUsuarioAndIdPartido(Long idUsuario, Long idPartido);
    boolean existsByIdUsuarioAndIdPartido(Long idUsuario, Long idPartido);
}
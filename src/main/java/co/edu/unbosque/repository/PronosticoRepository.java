package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Pronostico;

@Repository
public interface PronosticoRepository extends CrudRepository<Pronostico, Long> {
    List<Pronostico> findByIdPolla(Long idPolla);
    List<Pronostico> findByIdUsuario(Long idUsuario);
    List<Pronostico> findByIdPartido(Long idPartido);
    List<Pronostico> findByIdPollaAndIdUsuario(Long idPolla, Long idUsuario);
    Optional<Pronostico> findByIdPollaAndIdUsuarioAndIdPartido(Long idPolla, Long idUsuario, Long idPartido);
}
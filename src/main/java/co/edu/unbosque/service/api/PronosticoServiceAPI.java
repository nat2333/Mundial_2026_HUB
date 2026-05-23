package co.edu.unbosque.service.api;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.entity.Pronostico;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface PronosticoServiceAPI extends GenericServiceAPI<Pronostico, Long> {
    List<Pronostico> findByIdPolla(Long idPolla);
    List<Pronostico> findByIdUsuario(Long idUsuario);
    List<Pronostico> findByIdPartido(Long idPartido);
    List<Pronostico> findByIdPollaAndIdUsuario(Long idPolla, Long idUsuario);
    Optional<Pronostico> findByIdPollaAndIdUsuarioAndIdPartido(Long idPolla, Long idUsuario, Long idPartido);
}
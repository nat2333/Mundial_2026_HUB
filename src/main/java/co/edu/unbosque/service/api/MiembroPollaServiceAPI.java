package co.edu.unbosque.service.api;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.entity.MiembroPolla;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface MiembroPollaServiceAPI extends GenericServiceAPI<MiembroPolla, Long> {
    List<MiembroPolla> findByIdPolla(Long idPolla);
    List<MiembroPolla> findByIdUsuario(Long idUsuario);
    Optional<MiembroPolla> findByIdPollaAndIdUsuario(Long idPolla, Long idUsuario);
    List<MiembroPolla> findRankingByIdPolla(Long idPolla);
}
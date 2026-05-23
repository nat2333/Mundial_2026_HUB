package co.edu.unbosque.service.api;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.entity.Polla;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface PollaServiceAPI extends GenericServiceAPI<Polla, Long> {
    Optional<Polla> findByCodigoInvitacion(String codigoInvitacion);
    List<Polla> findByIdCreador(Long idCreador);
    List<Polla> findByEstado(String estado);
}
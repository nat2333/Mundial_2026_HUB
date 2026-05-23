package co.edu.unbosque.service.api;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.entity.LaminaUsuario;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface LaminaUsuarioServiceAPI extends GenericServiceAPI<LaminaUsuario, Long> {
    List<LaminaUsuario> findByIdUsuario(Long idUsuario);
    List<LaminaUsuario> findByIdLamina(Long idLamina);
    Optional<LaminaUsuario> findByIdUsuarioAndIdLamina(Long idUsuario, Long idLamina);
    List<LaminaUsuario> findByIdUsuarioAndEnIntercambio(Long idUsuario, byte enIntercambio);
}
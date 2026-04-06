package co.edu.unbosque.service.api;

import java.util.Optional;
import co.edu.unbosque.entity.PreferenciaUsuario;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface PreferenciaUsuarioServiceAPI extends GenericServiceAPI<PreferenciaUsuario, Long> {
    Optional<PreferenciaUsuario> findByIdUsuario(Long idUsuario);
}
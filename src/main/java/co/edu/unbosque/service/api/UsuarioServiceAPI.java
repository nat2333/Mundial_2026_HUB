package co.edu.unbosque.service.api;

import java.util.Optional;

import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface UsuarioServiceAPI extends GenericServiceAPI<Usuario, Long> {
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);
}
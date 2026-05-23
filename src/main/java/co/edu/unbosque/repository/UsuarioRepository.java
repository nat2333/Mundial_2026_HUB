package co.edu.unbosque.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Usuario;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);
    Optional<Usuario> findByCorreoUsuarioAndEstado(String correoUsuario, byte estado);
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);
}
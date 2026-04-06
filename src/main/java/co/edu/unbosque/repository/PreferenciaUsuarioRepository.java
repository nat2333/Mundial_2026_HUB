package co.edu.unbosque.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.entity.PreferenciaUsuario;

@Repository
public interface PreferenciaUsuarioRepository extends CrudRepository<PreferenciaUsuario, Long> {
    Optional<PreferenciaUsuario> findByIdUsuario(Long idUsuario);
}
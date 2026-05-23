package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.LaminaUsuario;

@Repository
public interface LaminaUsuarioRepository extends CrudRepository<LaminaUsuario, Long> {
    List<LaminaUsuario> findByIdUsuario(Long idUsuario);
    List<LaminaUsuario> findByIdLamina(Long idLamina);
    Optional<LaminaUsuario> findByIdUsuarioAndIdLamina(Long idUsuario, Long idLamina);
    List<LaminaUsuario> findByIdUsuarioAndEnIntercambio(Long idUsuario, byte enIntercambio);
}
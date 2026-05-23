package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.PaqueteLaminas;

@Repository
public interface PaqueteLaminasRepository extends CrudRepository<PaqueteLaminas, Long> {
    List<PaqueteLaminas> findByIdUsuario(Long idUsuario);
    List<PaqueteLaminas> findByIdUsuarioAndAbierto(Long idUsuario, byte abierto);
}
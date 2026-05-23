package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.MiembroPolla;

@Repository
public interface MiembroPollaRepository extends CrudRepository<MiembroPolla, Long> {
    List<MiembroPolla> findByIdPolla(Long idPolla);
    List<MiembroPolla> findByIdUsuario(Long idUsuario);
    Optional<MiembroPolla> findByIdPollaAndIdUsuario(Long idPolla, Long idUsuario);
    List<MiembroPolla> findByIdPollaOrderByPuntajeTotalDesc(Long idPolla);
}
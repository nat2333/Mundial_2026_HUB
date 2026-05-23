package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Polla;

@Repository
public interface PollaRepository extends CrudRepository<Polla, Long> {
    Optional<Polla> findByCodigoInvitacion(String codigoInvitacion);
    List<Polla> findByIdCreador(Long idCreador);
    List<Polla> findByEstado(String estado);
}
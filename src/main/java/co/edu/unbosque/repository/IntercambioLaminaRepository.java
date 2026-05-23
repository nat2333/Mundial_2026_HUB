package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.IntercambioLamina;

@Repository
public interface IntercambioLaminaRepository extends CrudRepository<IntercambioLamina, Long> {
    List<IntercambioLamina> findByIdUsuarioOferta(Long idUsuarioOferta);
    List<IntercambioLamina> findByIdUsuarioReceptor(Long idUsuarioReceptor);
    List<IntercambioLamina> findByEstado(String estado);
}
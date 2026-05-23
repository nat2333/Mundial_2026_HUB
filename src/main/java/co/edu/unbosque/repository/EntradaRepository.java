package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Entrada;

@Repository
public interface EntradaRepository extends CrudRepository<Entrada, Long> {
    List<Entrada> findByIdPartido(Long idPartido);
    List<Entrada> findByIdTitular(Long idTitular);
    List<Entrada> findByEstado(String estado);
    List<Entrada> findByIdPartidoAndEstado(Long idPartido, String estado);
}
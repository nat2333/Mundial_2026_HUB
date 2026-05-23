package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.DiscrepanciaDatos;

@Repository
public interface DiscrepanciaDatosRepository extends CrudRepository<DiscrepanciaDatos, Long> {
    List<DiscrepanciaDatos> findByIdPartido(Long idPartido);
    List<DiscrepanciaDatos> findByResuelto(byte resuelto);
}
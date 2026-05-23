package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.CasoSoporte;

@Repository
public interface CasoSoporteRepository extends CrudRepository<CasoSoporte, Long> {
    List<CasoSoporte> findByIdUsuario(Long idUsuario);
    List<CasoSoporte> findByIdAgente(Long idAgente);
    List<CasoSoporte> findByEstado(String estado);
    List<CasoSoporte> findByTipoCaso(String tipoCaso);
}
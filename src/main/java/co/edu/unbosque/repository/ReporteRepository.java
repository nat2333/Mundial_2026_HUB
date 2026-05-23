package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Reporte;

@Repository
public interface ReporteRepository extends CrudRepository<Reporte, Long> {
    List<Reporte> findByTipoReporte(String tipoReporte);
    List<Reporte> findByIdUsuarioGenera(Long idUsuarioGenera);
}
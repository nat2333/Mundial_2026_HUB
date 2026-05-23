package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.Reporte;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface ReporteServiceAPI extends GenericServiceAPI<Reporte, Long> {
    List<Reporte> findByTipoReporte(String tipoReporte);
    List<Reporte> findByIdUsuarioGenera(Long idUsuarioGenera);
}
package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Reporte;
import co.edu.unbosque.repository.ReporteRepository;
import co.edu.unbosque.service.api.ReporteServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class ReporteServiceImpl extends GenericServiceImpl<Reporte, Long>
        implements ReporteServiceAPI {

    @Autowired
    private ReporteRepository reporteRepository;

    @Override
    public CrudRepository<Reporte, Long> getDao() {
        return reporteRepository;
    }

    @Override
    public List<Reporte> findByTipoReporte(String tipoReporte) {
        return reporteRepository.findByTipoReporte(tipoReporte);
    }

    @Override
    public List<Reporte> findByIdUsuarioGenera(Long idUsuarioGenera) {
        return reporteRepository.findByIdUsuarioGenera(idUsuarioGenera);
    }
}
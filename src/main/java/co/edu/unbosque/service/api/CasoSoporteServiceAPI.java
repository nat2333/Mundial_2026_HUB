package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.CasoSoporte;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface CasoSoporteServiceAPI extends GenericServiceAPI<CasoSoporte, Long> {
    List<CasoSoporte> findByIdUsuario(Long idUsuario);
    List<CasoSoporte> findByIdAgente(Long idAgente);
    List<CasoSoporte> findByEstado(String estado);
    List<CasoSoporte> findByTipoCaso(String tipoCaso);
}
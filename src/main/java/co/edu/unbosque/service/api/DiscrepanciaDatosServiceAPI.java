package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.DiscrepanciaDatos;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface DiscrepanciaDatosServiceAPI extends GenericServiceAPI<DiscrepanciaDatos, Long> {
    List<DiscrepanciaDatos> findByIdPartido(Long idPartido);
    List<DiscrepanciaDatos> findByResuelto(byte resuelto);
}
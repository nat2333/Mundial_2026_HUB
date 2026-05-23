package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface EntradaServiceAPI extends GenericServiceAPI<Entrada, Long> {
    List<Entrada> findByIdPartido(Long idPartido);
    List<Entrada> findByIdTitular(Long idTitular);
    List<Entrada> findByEstado(String estado);
    List<Entrada> findByIdPartidoAndEstado(Long idPartido, String estado);
}
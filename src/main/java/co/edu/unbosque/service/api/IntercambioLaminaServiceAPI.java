package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.IntercambioLamina;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface IntercambioLaminaServiceAPI extends GenericServiceAPI<IntercambioLamina, Long> {
    List<IntercambioLamina> findByIdUsuarioOferta(Long idUsuarioOferta);
    List<IntercambioLamina> findByIdUsuarioReceptor(Long idUsuarioReceptor);
    List<IntercambioLamina> findByEstado(String estado);
}
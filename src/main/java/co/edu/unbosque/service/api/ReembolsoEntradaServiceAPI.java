package co.edu.unbosque.service.api;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.entity.ReembolsoEntrada;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface ReembolsoEntradaServiceAPI extends GenericServiceAPI<ReembolsoEntrada, Long> {
    List<ReembolsoEntrada> findByIdEntrada(Long idEntrada);
    List<ReembolsoEntrada> findByIdUsuario(Long idUsuario);
    List<ReembolsoEntrada> findByEstado(String estado);
    Optional<ReembolsoEntrada> findByIdTransaccionReembolso(String idTransaccionReembolso);
}
package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.TransferenciaEntrada;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface TransferenciaEntradaServiceAPI extends GenericServiceAPI<TransferenciaEntrada, Long> {
    List<TransferenciaEntrada> findByIdEntrada(Long idEntrada);
    List<TransferenciaEntrada> findByIdUsuarioOrigen(Long idUsuarioOrigen);
    List<TransferenciaEntrada> findByIdUsuarioDestino(Long idUsuarioDestino);
}
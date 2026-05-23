package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.TransferenciaEntrada;

@Repository
public interface TransferenciaEntradaRepository extends CrudRepository<TransferenciaEntrada, Long> {
    List<TransferenciaEntrada> findByIdEntrada(Long idEntrada);
    List<TransferenciaEntrada> findByIdUsuarioOrigen(Long idUsuarioOrigen);
    List<TransferenciaEntrada> findByIdUsuarioDestino(Long idUsuarioDestino);
}
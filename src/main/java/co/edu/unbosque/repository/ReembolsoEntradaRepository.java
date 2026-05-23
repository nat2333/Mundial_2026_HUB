package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.ReembolsoEntrada;

@Repository
public interface ReembolsoEntradaRepository extends CrudRepository<ReembolsoEntrada, Long> {
    List<ReembolsoEntrada> findByIdEntrada(Long idEntrada);
    List<ReembolsoEntrada> findByIdUsuario(Long idUsuario);
    List<ReembolsoEntrada> findByEstado(String estado);
    java.util.Optional<ReembolsoEntrada> findByIdTransaccionReembolso(String idTransaccionReembolso);
}
package co.edu.unbosque.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.FacturaEntrada;

@Repository
public interface FacturaEntradaRepository extends JpaRepository<FacturaEntrada, Long> {

    Optional<FacturaEntrada> findByIdEntrada(Long idEntrada);

    Optional<FacturaEntrada> findByNumeroFactura(String numeroFactura);

    boolean existsByIdEntrada(Long idEntrada);
}

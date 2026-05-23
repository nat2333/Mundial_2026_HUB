package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Lamina;

@Repository
public interface LaminaRepository extends CrudRepository<Lamina, Long> {
    List<Lamina> findBySeleccion(String seleccion);
    List<Lamina> findByCategoria(String categoria);
    List<Lamina> findByRareza(String rareza);
}
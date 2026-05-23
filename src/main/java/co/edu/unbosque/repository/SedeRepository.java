package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Sede;

@Repository
public interface SedeRepository extends CrudRepository<Sede, Long> {

    List<Sede> findByPaisIgnoreCase(String pais);

    List<Sede> findByCiudadIgnoreCase(String ciudad);

    List<Sede> findByNombreEstadioContainingIgnoreCase(String nombre);

    java.util.Optional<Sede> findByNombreEstadioIgnoreCase(String nombreEstadio);
}

package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Partido;

@Repository
public interface PartidoRepository extends CrudRepository<Partido, Long> {
    List<Partido> findByEstadoOrderByFechaHoraAsc(String estado);
    List<Partido> findBySede_CiudadIgnoreCase(String ciudad);
    List<Partido> findBySede_NombreEstadioIgnoreCase(String nombreEstadio);
    List<Partido> findByEquipoLocalOrEquipoVisitante(String equipoLocal, String equipoVisitante);
    Optional<Partido> findByIdExterno(String idExterno);
}
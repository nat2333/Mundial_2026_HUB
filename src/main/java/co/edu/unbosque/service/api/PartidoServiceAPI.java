package co.edu.unbosque.service.api;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface PartidoServiceAPI extends GenericServiceAPI<Partido, Long> {
    List<Partido> findByEstado(String estado);
    List<Partido> findByCiudad(String ciudad);
    List<Partido> findByEstadio(String estadio);
    List<Partido> findByEquipo(String equipo);
    Optional<Partido> findByIdExterno(String idExterno);
}
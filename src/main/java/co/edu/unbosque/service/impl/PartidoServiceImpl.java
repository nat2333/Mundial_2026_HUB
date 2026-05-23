package co.edu.unbosque.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.repository.PartidoRepository;
import co.edu.unbosque.service.api.PartidoServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class PartidoServiceImpl extends GenericServiceImpl<Partido, Long>
        implements PartidoServiceAPI {

    @Autowired
    private PartidoRepository partidoRepository;

    @Override
    public CrudRepository<Partido, Long> getDao() {
        return partidoRepository;
    }

    @Override
    public List<Partido> findByEstado(String estado) {
        return partidoRepository.findByEstadoOrderByFechaHoraAsc(estado);
    }

    @Override
    public List<Partido> findByCiudad(String ciudad) {
        return partidoRepository.findBySede_CiudadIgnoreCase(ciudad);
    }

    @Override
    public List<Partido> findByEstadio(String estadio) {
        return partidoRepository.findBySede_NombreEstadioIgnoreCase(estadio);
    }

    @Override
    public List<Partido> findByEquipo(String equipo) {
        return partidoRepository.findByEquipoLocalOrEquipoVisitante(equipo, equipo);
    }

    @Override
    public Optional<Partido> findByIdExterno(String idExterno) {
        return partidoRepository.findByIdExterno(idExterno);
    }
}
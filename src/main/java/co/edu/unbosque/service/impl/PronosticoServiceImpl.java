package co.edu.unbosque.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Pronostico;
import co.edu.unbosque.repository.PronosticoRepository;
import co.edu.unbosque.service.api.PronosticoServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class PronosticoServiceImpl extends GenericServiceImpl<Pronostico, Long>
        implements PronosticoServiceAPI {

    @Autowired
    private PronosticoRepository pronosticoRepository;

    @Override
    public CrudRepository<Pronostico, Long> getDao() {
        return pronosticoRepository;
    }

    @Override
    public List<Pronostico> findByIdPolla(Long idPolla) {
        return pronosticoRepository.findByIdPolla(idPolla);
    }

    @Override
    public List<Pronostico> findByIdUsuario(Long idUsuario) {
        return pronosticoRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<Pronostico> findByIdPartido(Long idPartido) {
        return pronosticoRepository.findByIdPartido(idPartido);
    }

    @Override
    public List<Pronostico> findByIdPollaAndIdUsuario(Long idPolla, Long idUsuario) {
        return pronosticoRepository.findByIdPollaAndIdUsuario(idPolla, idUsuario);
    }

    @Override
    public Optional<Pronostico> findByIdPollaAndIdUsuarioAndIdPartido(
            Long idPolla, Long idUsuario, Long idPartido) {
        return pronosticoRepository.findByIdPollaAndIdUsuarioAndIdPartido(
            idPolla, idUsuario, idPartido);
    }
}
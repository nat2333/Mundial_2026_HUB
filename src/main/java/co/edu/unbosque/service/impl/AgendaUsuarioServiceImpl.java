package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.AgendaUsuario;
import co.edu.unbosque.repository.AgendaUsuarioRepository;
import co.edu.unbosque.service.api.AgendaUsuarioServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class AgendaUsuarioServiceImpl extends GenericServiceImpl<AgendaUsuario, Long>
        implements AgendaUsuarioServiceAPI {

    @Autowired
    private AgendaUsuarioRepository agendaUsuarioRepository;

    @Override
    public CrudRepository<AgendaUsuario, Long> getDao() {
        return agendaUsuarioRepository;
    }

    @Override
    public List<AgendaUsuario> findByIdUsuario(Long idUsuario) {
        return agendaUsuarioRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<AgendaUsuario> findByIdPartido(Long idPartido) {
        return agendaUsuarioRepository.findByIdPartido(idPartido);
    }

    @Override
    public boolean existeDuplicado(Long idUsuario, Long idPartido) {
        return agendaUsuarioRepository.existsByIdUsuarioAndIdPartido(idUsuario, idPartido);
    }
}
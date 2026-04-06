package co.edu.unbosque.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.repository.EventoAuditoriaRepository;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class EventoAuditoriaServiceImpl extends GenericServiceImpl<EventoAuditoria, Long>
        implements EventoAuditoriaServiceAPI {

    @Autowired
    private EventoAuditoriaRepository eventoAuditoriaRepository;

    @Override
    public CrudRepository<EventoAuditoria, Long> getDao() {
        return eventoAuditoriaRepository;
    }

    @Override
    public List<EventoAuditoria> findByIdUsuario(Long idUsuario) {
        return eventoAuditoriaRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<EventoAuditoria> findByIdCorrelacion(String idCorrelacion) {
        return eventoAuditoriaRepository.findByIdCorrelacion(idCorrelacion);
    }
}
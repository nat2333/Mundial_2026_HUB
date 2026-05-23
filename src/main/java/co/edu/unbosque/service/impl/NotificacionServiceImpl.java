package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Notificacion;
import co.edu.unbosque.repository.NotificacionRepository;
import co.edu.unbosque.service.api.NotificacionServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class NotificacionServiceImpl extends GenericServiceImpl<Notificacion, Long>
        implements NotificacionServiceAPI {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Override
    public CrudRepository<Notificacion, Long> getDao() {
        return notificacionRepository;
    }

    @Override
    public List<Notificacion> findByIdUsuario(Long idUsuario) {
        return notificacionRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<Notificacion> findByIdPartido(Long idPartido) {
        return notificacionRepository.findByIdPartido(idPartido);
    }

    @Override
    public List<Notificacion> findByIdCorrelacion(String idCorrelacion) {
        return notificacionRepository.findByIdCorrelacion(idCorrelacion);
    }
}
package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.Notificacion;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface NotificacionServiceAPI extends GenericServiceAPI<Notificacion, Long> {
    List<Notificacion> findByIdUsuario(Long idUsuario);
    List<Notificacion> findByIdPartido(Long idPartido);
    List<Notificacion> findByIdCorrelacion(String idCorrelacion);
}
package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.Notificacion;

@Repository
public interface NotificacionRepository extends CrudRepository<Notificacion, Long> {
    List<Notificacion> findByIdUsuario(Long idUsuario);
    List<Notificacion> findByIdPartido(Long idPartido);
    List<Notificacion> findByIdCorrelacion(String idCorrelacion);
}
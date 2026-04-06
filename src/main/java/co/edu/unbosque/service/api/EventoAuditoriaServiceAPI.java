package co.edu.unbosque.service.api;

import java.util.List;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface EventoAuditoriaServiceAPI extends GenericServiceAPI<EventoAuditoria, Long> {
    List<EventoAuditoria> findByIdUsuario(Long idUsuario);
    List<EventoAuditoria> findByIdCorrelacion(String idCorrelacion);
}
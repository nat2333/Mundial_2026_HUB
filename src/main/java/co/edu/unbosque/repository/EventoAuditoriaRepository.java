package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.entity.EventoAuditoria;

@Repository
public interface EventoAuditoriaRepository extends CrudRepository<EventoAuditoria, Long> {
    List<EventoAuditoria> findByIdUsuario(Long idUsuario);
    List<EventoAuditoria> findByIdCorrelacion(String idCorrelacion);
}
package co.edu.unbosque.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;

@RestController
@RequestMapping("/auditoria")
public class EventoAuditoriaRestController {

    private static final Logger logger = Logger.getLogger(EventoAuditoriaRestController.class.getName());

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    // HU-28: Ver todos los eventos
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        logger.info("Consultando todos los eventos de auditoría");
        return ResponseEntity.ok(eventoAuditoriaServiceAPI.getAll());
    }

    // HU-28: Ver eventos por ID de usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getByUsuario(@PathVariable Long idUsuario) {
        logger.info("Consultando eventos del usuario: " + idUsuario);
        List<EventoAuditoria> eventos =
                eventoAuditoriaServiceAPI.findByIdUsuario(idUsuario);
        if (eventos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay eventos para este usuario."));
        }
        return ResponseEntity.ok(eventos);
    }

    // HU-29: Buscar por ID de correlación
    @GetMapping("/correlacion/{idCorrelacion}")
    public ResponseEntity<?> getByCorrelacion(@PathVariable String idCorrelacion) {
        logger.info("Buscando eventos con correlación: " + idCorrelacion);
        List<EventoAuditoria> eventos =
                eventoAuditoriaServiceAPI.findByIdCorrelacion(idCorrelacion);
        if (eventos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No se encontraron eventos con ese ID de correlación."));
        }
        return ResponseEntity.ok(eventos);
    }

    // GET evento por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        EventoAuditoria evento = eventoAuditoriaServiceAPI.get(id);
        if (evento == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Evento no encontrado."));
        }
        return ResponseEntity.ok(evento);
    }
}
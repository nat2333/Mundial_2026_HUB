package co.edu.unbosque.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.AgendaUsuario;
import co.edu.unbosque.service.api.AgendaUsuarioServiceAPI;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/agenda")
public class AgendaUsuarioRestController {

    private static final Logger logger = Logger.getLogger(AgendaUsuarioRestController.class.getName());

    @Autowired
    private AgendaUsuarioServiceAPI agendaUsuarioServiceAPI;

    // HU-06: Ver agenda personal del usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getAgendaUsuario(@PathVariable Long idUsuario) {
        logger.info("Consultando agenda del usuario: " + idUsuario);
        List<AgendaUsuario> agenda = agendaUsuarioServiceAPI.findByIdUsuario(idUsuario);
        if (agenda.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos en la agenda."));
        }
        return ResponseEntity.ok(agenda);
    }

    // HU-06: Agregar partido a la agenda (idempotente — rechaza duplicados)
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarAAgenda(@RequestBody AgendaUsuario agenda) {
        if (agenda.getIdUsuario() == null || agenda.getIdPartido() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "idUsuario e idPartido son obligatorios."));
        }
        if (agendaUsuarioServiceAPI.existeDuplicado(agenda.getIdUsuario(), agenda.getIdPartido())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El partido ya está en la agenda de este usuario."));
        }
        agenda.setFechaAgendado(new Date());
        AgendaUsuario guardado = agendaUsuarioServiceAPI.save(agenda);
        logger.info("Partido " + agenda.getIdPartido() + " agendado para usuario " + agenda.getIdUsuario());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Partido agregado a la agenda.", "agenda", guardado));
    }

    // HU-06: Eliminar partido de la agenda
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDeAgenda(@PathVariable Long id) {
        AgendaUsuario agenda = agendaUsuarioServiceAPI.get(id);
        if (agenda == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Registro no encontrado."));
        }
        agendaUsuarioServiceAPI.delete(id);
        return ResponseEntity.ok(Map.of("mensaje", "Partido eliminado de la agenda."));
    }

    // GET todos
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(agendaUsuarioServiceAPI.getAll());
    }
}
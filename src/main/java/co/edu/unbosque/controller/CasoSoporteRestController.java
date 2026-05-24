package co.edu.unbosque.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.CasoSoporte;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.service.api.CasoSoporteServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/soporte")
public class CasoSoporteRestController {

    private static final Logger logger = Logger.getLogger(CasoSoporteRestController.class.getName());

    @Autowired
    private CasoSoporteServiceAPI casoSoporteServiceAPI;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    // HU-23: Abrir caso de soporte
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaso(@RequestBody CasoSoporte caso, HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Abriendo caso de soporte tipo: " + caso.getTipoCaso());

            caso.setEstado("ABIERTO");
            caso.setFechaApertura(new Date());
            caso.setIdCorrelacion(correlacion);
            CasoSoporte guardado = casoSoporteServiceAPI.save(caso);

            // Registrar en auditoría
            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(caso.getIdUsuario());
            evento.setTipoEvento("CASO_SOPORTE_ABIERTO");
            evento.setModuloOrigen("BACKOFFICE");
            evento.setDetalle("Caso abierto tipo: " + caso.getTipoCaso());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Caso de soporte abierto.",
                        "caso", guardado,
                        "idCorrelacion", correlacion
                    ));
        } catch (Exception e) {
            logger.severe("Error abriendo caso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-23: Asignar agente al caso
    @PutMapping("/asignar/{idCaso}/{idAgente}")
    public ResponseEntity<?> asignarAgente(
            @PathVariable Long idCaso,
            @PathVariable Long idAgente) {
        CasoSoporte caso = casoSoporteServiceAPI.get(idCaso);
        if (caso == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Caso no encontrado."));
        }
        caso.setIdAgente(idAgente);
        caso.setEstado("EN_PROCESO");
        casoSoporteServiceAPI.save(caso);
        return ResponseEntity.ok(Map.of("mensaje", "Agente asignado al caso.", "caso", caso));
    }

    // HU-23: Cerrar caso
    @PutMapping("/cerrar/{idCaso}")
    public ResponseEntity<?> cerrarCaso(@PathVariable Long idCaso, HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            CasoSoporte caso = casoSoporteServiceAPI.get(idCaso);
            if (caso == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Caso no encontrado."));
            }
            caso.setEstado("CERRADO");
            caso.setFechaCierre(new Date());
            casoSoporteServiceAPI.save(caso);

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(caso.getIdUsuario());
            evento.setTipoEvento("CASO_SOPORTE_CERRADO");
            evento.setModuloOrigen("BACKOFFICE");
            evento.setDetalle("Caso cerrado ID: " + idCaso);
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            return ResponseEntity.ok(Map.of("mensaje", "Caso cerrado exitosamente.", "caso", caso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-29: Ver historial de eventos de un usuario
    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<?> getHistorialUsuario(@PathVariable Long idUsuario) {
        logger.info("Consultando historial del usuario: " + idUsuario);
        List<EventoAuditoria> eventos = eventoAuditoriaServiceAPI.findByIdUsuario(idUsuario);
        List<CasoSoporte> casos = casoSoporteServiceAPI.findByIdUsuario(idUsuario);
        if (eventos.isEmpty() && casos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay historial para este usuario."));
        }
        return ResponseEntity.ok(Map.of(
            "eventos", eventos,
            "casos", casos,
            "totalEventos", eventos.size(),
            "totalCasos", casos.size()
        ));
    }

    // GET casos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> getByEstado(@PathVariable String estado) {
        List<CasoSoporte> casos = casoSoporteServiceAPI.findByEstado(estado);
        if (casos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay casos con estado " + estado));
        }
        return ResponseEntity.ok(casos);
    }

    // GET todos los casos
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(casoSoporteServiceAPI.getAll());
    }

    // GET caso por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        CasoSoporte caso = casoSoporteServiceAPI.get(id);
        if (caso == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Caso no encontrado."));
        }
        return ResponseEntity.ok(caso);
    }
}
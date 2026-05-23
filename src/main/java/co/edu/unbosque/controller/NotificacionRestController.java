package co.edu.unbosque.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Notificacion;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.NotificacionServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.service.impl.EmailService;
import co.edu.unbosque.utils.Utilidad;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/notificacion")
public class NotificacionRestController {

    private static final Logger logger = Logger.getLogger(NotificacionRestController.class.getName());

    @Autowired
    private NotificacionServiceAPI notificacionServiceAPI;

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private EmailService emailService;

    // HU-08: Notificación de inicio de partido
    @PostMapping("/inicioPartido")
    public ResponseEntity<?> notificarInicioPartido(@RequestBody Notificacion notificacion) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Enviando notificacion de inicio de partido");
            notificacion.setTipo("INICIO_PARTIDO");
            notificacion.setFechaEnvio(new Date());
            notificacion.setEstadoEnvio("ENVIADA");
            notificacion.setIdCorrelacion(correlacion);
            Notificacion guardada = notificacionServiceAPI.save(notificacion);

            // Enviar por email si canal es EMAIL
            if ("EMAIL".equalsIgnoreCase(notificacion.getCanal())
                    && notificacion.getIdUsuario() != null) {
                Usuario usuario = usuarioServiceAPI.get(notificacion.getIdUsuario());
                if (usuario != null) {
                    emailService.enviarNotificacion(
                        usuario.getCorreoUsuario(),
                        usuario.getNombres(),
                        "Inicio de Partido",
                        notificacion.getMensaje()
                    );
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Notificacion de inicio enviada.", "notificacion", guardada));
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-09: Notificación de gol
    @PostMapping("/gol")
    public ResponseEntity<?> notificarGol(@RequestBody Notificacion notificacion) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Enviando notificacion de gol");
            notificacion.setTipo("GOL");
            notificacion.setFechaEnvio(new Date());
            notificacion.setEstadoEnvio("ENVIADA");
            notificacion.setIdCorrelacion(correlacion);
            Notificacion guardada = notificacionServiceAPI.save(notificacion);

            if ("EMAIL".equalsIgnoreCase(notificacion.getCanal())
                    && notificacion.getIdUsuario() != null) {
                Usuario usuario = usuarioServiceAPI.get(notificacion.getIdUsuario());
                if (usuario != null) {
                    emailService.enviarNotificacion(
                        usuario.getCorreoUsuario(),
                        usuario.getNombres(),
                        "Gol",
                        notificacion.getMensaje()
                    );
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Notificacion de gol enviada.", "notificacion", guardada));
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-10: Notificación de cambio de horario
    @PostMapping("/cambioHorario")
    public ResponseEntity<?> notificarCambioHorario(@RequestBody Notificacion notificacion) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Enviando notificacion de cambio de horario");
            notificacion.setTipo("CAMBIO_HORARIO");
            notificacion.setFechaEnvio(new Date());
            notificacion.setEstadoEnvio("ENVIADA");
            notificacion.setIdCorrelacion(correlacion);
            Notificacion guardada = notificacionServiceAPI.save(notificacion);

            if ("EMAIL".equalsIgnoreCase(notificacion.getCanal())
                    && notificacion.getIdUsuario() != null) {
                Usuario usuario = usuarioServiceAPI.get(notificacion.getIdUsuario());
                if (usuario != null) {
                    emailService.enviarNotificacion(
                        usuario.getCorreoUsuario(),
                        usuario.getNombres(),
                        "Cambio de Horario",
                        notificacion.getMensaje()
                    );
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Notificacion de cambio enviada.", "notificacion", guardada));
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-11: Notificación masiva por operador
    @PostMapping("/masiva")
    public ResponseEntity<?> notificacionMasiva(@RequestBody Notificacion notificacion) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Enviando notificacion masiva");
            notificacion.setTipo("MASIVA");
            notificacion.setFechaEnvio(new Date());
            notificacion.setEstadoEnvio("ENVIADA");
            notificacion.setIdCorrelacion(correlacion);
            Notificacion guardada = notificacionServiceAPI.save(notificacion);

            // Enviar a todos los usuarios activos
            if ("EMAIL".equalsIgnoreCase(notificacion.getCanal())) {
                usuarioServiceAPI.getAll().forEach(usuario -> {
                    if (usuario.getEstado() == 1 && usuario.isCorreoVerificado()) {
                        try {
                            emailService.enviarNotificacion(
                                usuario.getCorreoUsuario(),
                                usuario.getNombres(),
                                "Comunicado Mundial 2026 Hub",
                                notificacion.getMensaje()
                            );
                        } catch (Exception e) {
                            logger.warning("Error enviando a " + usuario.getCorreoUsuario());
                        }
                    }
                });
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Notificacion masiva enviada.", "notificacion", guardada));
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // GET todas
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(notificacionServiceAPI.getAll());
    }

    // GET por usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getByUsuario(@PathVariable Long idUsuario) {
        List<Notificacion> notificaciones = notificacionServiceAPI.findByIdUsuario(idUsuario);
        if (notificaciones.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay notificaciones para este usuario."));
        }
        return ResponseEntity.ok(notificaciones);
    }

    // GET por partido
    @GetMapping("/partido/{idPartido}")
    public ResponseEntity<?> getByPartido(@PathVariable Long idPartido) {
        List<Notificacion> notificaciones = notificacionServiceAPI.findByIdPartido(idPartido);
        if (notificaciones.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay notificaciones para este partido."));
        }
        return ResponseEntity.ok(notificaciones);
    }

    // GET por correlacion
    @GetMapping("/correlacion/{idCorrelacion}")
    public ResponseEntity<?> getByCorrelacion(@PathVariable String idCorrelacion) {
        List<Notificacion> notificaciones = notificacionServiceAPI.findByIdCorrelacion(idCorrelacion);
        if (notificaciones.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay notificaciones con ese ID de correlacion."));
        }
        return ResponseEntity.ok(notificaciones);
    }
}
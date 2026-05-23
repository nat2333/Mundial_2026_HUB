package co.edu.unbosque.controller;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.entity.ReembolsoEntrada;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.ReembolsoEntradaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.service.impl.EmailService;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/reembolso")
public class ReembolsoEntradaRestController {

    private static final Logger logger = Logger.getLogger(ReembolsoEntradaRestController.class.getName());

    @Autowired
    private ReembolsoEntradaServiceAPI reembolsoEntradaServiceAPI;

    @Autowired
    private EntradaServiceAPI entradaServiceAPI;

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    private void registrarAuditoria(String correlacion, Long idUsuario, Long idReembolso,
                                    String tipoEvento, String detalle, String estado,
                                    HttpServletRequest request) {
        try {
            EventoAuditoria ev = new EventoAuditoria();
            ev.setIdCorrelacion(correlacion);
            ev.setIdUsuario(idUsuario);
            ev.setTipoEvento(tipoEvento);
            ev.setModuloOrigen("REEMBOLSO");
            ev.setDetalle(detalle);
            ev.setTimestampEvento(new Date());
            ev.setEstadoResultado(estado);
            ev.setEntidadAfectada("ReembolsoEntrada");
            ev.setIdEntidad(idReembolso);
            ev.setIpOrigen(Utilidad.obtenerIp(request));
            eventoAuditoriaServiceAPI.save(ev);
        } catch (Exception e) {
            logger.warning("No se pudo registrar auditoría reembolso: " + e.getMessage());
        }
    }

    private void notificarUsuario(Long idUsuario, String tipo, String mensaje) {
        try {
            Usuario usuario = usuarioServiceAPI.get(idUsuario);
            if (usuario == null) {
				return;
			}
            emailService.enviarNotificacion(
                usuario.getCorreoUsuario(),
                usuario.getNombres() != null ? usuario.getNombres() : "Usuario",
                tipo, mensaje
            );
        } catch (Exception e) {
            logger.warning("No se pudo enviar correo de reembolso a usuario " + idUsuario + ": " + e.getMessage());
        }
    }

    // HU-15: Solicitar reembolso
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarReembolso(@RequestBody ReembolsoEntrada reembolso,
                                                HttpServletRequest request) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Solicitando reembolso entrada: " + reembolso.getIdEntrada());

            Entrada entrada = entradaServiceAPI.get(reembolso.getIdEntrada());
            if (entrada == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Entrada no encontrada."));
            }

            if (!"PAGADA".equals(entrada.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "Solo se pueden reembolsar entradas PAGADAS."));
            }

            if (!entrada.getIdTitular().equals(reembolso.getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No eres el titular de esta entrada."));
            }

            reembolso.setEstado("PENDIENTE");
            reembolso.setFechaSolicitud(new Date());
            reembolso.setIdCorrelacion(correlacion);
            ReembolsoEntrada guardado = reembolsoEntradaServiceAPI.save(reembolso);

            registrarAuditoria(correlacion, guardado.getIdUsuario(), guardado.getId(),
                "REEMBOLSO_SOLICITADO",
                "Solicitud de reembolso entrada " + guardado.getIdEntrada()
                    + " | motivo: " + (guardado.getMotivo() == null ? "—" : guardado.getMotivo()),
                "OK", request);

            notificarUsuario(
                guardado.getIdUsuario(),
                "Solicitud de reembolso recibida",
                "Hemos recibido tu solicitud de reembolso para la entrada #" + guardado.getIdEntrada()
                    + ". Quedó en estado <strong>PENDIENTE</strong> y será revisada por el equipo. "
                    + "Te notificaremos por correo cuando haya una resolución."
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Solicitud de reembolso registrada.",
                        "reembolso", guardado,
                        "idCorrelacion", correlacion
                    ));
        } catch (Exception e) {
            logger.severe("Error solicitando reembolso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // Aprobar reembolso
    @PutMapping("/aprobar/{idReembolso}")
    public ResponseEntity<?> aprobarReembolso(
            @PathVariable Long idReembolso,
            @RequestParam String idTransaccionReembolso,
            HttpServletRequest request) {
        try {
            ReembolsoEntrada reembolso = reembolsoEntradaServiceAPI.get(idReembolso);
            if (reembolso == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Reembolso no encontrado."));
            }

            if (!"PENDIENTE".equals(reembolso.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El reembolso ya fue procesado."));
            }

            // Actualizar entrada
            Entrada entrada = entradaServiceAPI.get(reembolso.getIdEntrada());
            if (entrada != null) {
                entrada.setEstado("REEMBOLSADA");
                entradaServiceAPI.save(entrada);
            }

            reembolso.setEstado("APROBADO");
            reembolso.setFechaResolucion(new Date());
            reembolso.setIdTransaccionReembolso(idTransaccionReembolso);
            reembolsoEntradaServiceAPI.save(reembolso);

            String correlacion = reembolso.getIdCorrelacion() != null
                ? reembolso.getIdCorrelacion()
                : Utilidad.generarIdCorrelacion();
            registrarAuditoria(correlacion, reembolso.getIdUsuario(), reembolso.getId(),
                "REEMBOLSO_APROBADO",
                "Reembolso aprobado | entrada: " + reembolso.getIdEntrada()
                    + " | transacción: " + idTransaccionReembolso,
                "OK", request);

            notificarUsuario(
                reembolso.getIdUsuario(),
                "Reembolso aprobado",
                "Tu solicitud de reembolso para la entrada #" + reembolso.getIdEntrada()
                    + " fue <strong>aprobada</strong>. El monto será devuelto al medio de pago original. "
                    + "Referencia: " + idTransaccionReembolso + "."
            );

            return ResponseEntity.ok(Map.of(
                "mensaje", "Reembolso aprobado exitosamente.",
                "reembolso", reembolso
            ));
        } catch (Exception e) {
            logger.severe("Error aprobando reembolso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // Rechazar reembolso
    @PutMapping("/rechazar/{idReembolso}")
    public ResponseEntity<?> rechazarReembolso(
            @PathVariable Long idReembolso,
            @RequestParam(required = false) String motivo,
            HttpServletRequest request) {
        ReembolsoEntrada reembolso = reembolsoEntradaServiceAPI.get(idReembolso);
        if (reembolso == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Reembolso no encontrado."));
        }
        if (!"PENDIENTE".equals(reembolso.getEstado())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "El reembolso ya fue procesado."));
        }
        reembolso.setEstado("RECHAZADO");
        reembolso.setFechaResolucion(new Date());
        reembolsoEntradaServiceAPI.save(reembolso);

        String correlacion = reembolso.getIdCorrelacion() != null
            ? reembolso.getIdCorrelacion()
            : Utilidad.generarIdCorrelacion();
        registrarAuditoria(correlacion, reembolso.getIdUsuario(), reembolso.getId(),
            "REEMBOLSO_RECHAZADO",
            "Reembolso rechazado | entrada: " + reembolso.getIdEntrada()
                + (motivo != null ? " | motivo: " + motivo : ""),
            "OK", request);

        notificarUsuario(
            reembolso.getIdUsuario(),
            "Reembolso rechazado",
            "Tu solicitud de reembolso para la entrada #" + reembolso.getIdEntrada()
                + " fue <strong>rechazada</strong>."
                + (motivo != null ? " Motivo: " + motivo + "." : "")
                + " Si crees que es un error, contacta a soporte."
        );

        return ResponseEntity.ok(Map.of("mensaje", "Reembolso rechazado."));
    }

    // GET reembolsos por usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getByUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(reembolsoEntradaServiceAPI.findByIdUsuario(idUsuario));
    }

    // GET reembolsos pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<?> getPendientes() {
        return ResponseEntity.ok(reembolsoEntradaServiceAPI.findByEstado("PENDIENTE"));
    }

    // GET todos
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(reembolsoEntradaServiceAPI.getAll());
    }
}
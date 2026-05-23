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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.entity.TransferenciaEntrada;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.PartidoServiceAPI;
import co.edu.unbosque.service.api.TransferenciaEntradaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.service.impl.EmailService;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/transferencia")
public class TransferenciaEntradaRestController {

    private static final Logger logger = Logger.getLogger(TransferenciaEntradaRestController.class.getName());

    @Autowired private TransferenciaEntradaServiceAPI transferenciaEntradaServiceAPI;
    @Autowired private EntradaServiceAPI               entradaServiceAPI;
    @Autowired private UsuarioServiceAPI               usuarioServiceAPI;
    @Autowired private PartidoServiceAPI               partidoServiceAPI;
    @Autowired private EventoAuditoriaServiceAPI       eventoAuditoriaServiceAPI;
    @Autowired private EmailService                    emailService;

    // HU-14: Transferir entrada
    @PostMapping("/transferir")
    public ResponseEntity<?> transferirEntrada(@RequestBody TransferenciaEntrada transferencia,
                                               HttpServletRequest request) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Transfiriendo entrada: " + transferencia.getIdEntrada());

            // Validaciones
            Entrada entrada = entradaServiceAPI.get(transferencia.getIdEntrada());
            if (entrada == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Entrada no encontrada."));
            }
            if (!"PAGADA".equals(entrada.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "Solo se pueden transferir entradas PAGADAS."));
            }
            if (entrada.getIdTitular() == null
                    || !entrada.getIdTitular().equals(transferencia.getIdUsuarioOrigen())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No eres el titular de esta entrada."));
            }
            if (transferencia.getIdUsuarioDestino() == null
                    || transferencia.getIdUsuarioDestino().equals(transferencia.getIdUsuarioOrigen())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El usuario destino debe ser distinto al origen."));
            }

            Usuario origen  = usuarioServiceAPI.get(transferencia.getIdUsuarioOrigen());
            Usuario destino = usuarioServiceAPI.get(transferencia.getIdUsuarioDestino());
            if (origen == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Usuario origen no encontrado."));
            }
            if (destino == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Usuario destino no encontrado. Verifica el ID o correo."));
            }
            if (destino.getEstado() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El usuario destino está bloqueado."));
            }

            // Cambio de titularidad — la entrada sigue PAGADA, solo cambia el titular
            entrada.setIdTitular(destino.getId());
            entradaServiceAPI.save(entrada);

            // Registrar transferencia
            transferencia.setFechaTransferencia(new Date());
            transferencia.setIdCorrelacion(correlacion);
            transferencia.setEstado("COMPLETADA");
            TransferenciaEntrada guardada = transferenciaEntradaServiceAPI.save(transferencia);

            // Auditoría — un evento por cada usuario involucrado, mismo idCorrelacion
            registrarAuditoria(correlacion, origen.getId(), guardada.getId(),
                "ENTRADA_TRANSFERIDA_ORIGEN",
                "Entrada " + entrada.getId() + " transferida a usuario " + destino.getId()
                    + " (" + destino.getCorreoUsuario() + ")",
                "OK", request);
            registrarAuditoria(correlacion, destino.getId(), guardada.getId(),
                "ENTRADA_TRANSFERIDA_DESTINO",
                "Entrada " + entrada.getId() + " recibida de usuario " + origen.getId()
                    + " (" + origen.getCorreoUsuario() + ")",
                "OK", request);

            // Datos del partido para los correos
            Partido partido = null;
            try { partido = partidoServiceAPI.get(entrada.getIdPartido()); }
            catch (Exception ignored) {}
            String detallePartido = describirPartido(partido, entrada);

            // Correo al origen
            notificar(origen,
                "Entrada transferida",
                "Confirmamos que transferiste tu entrada <strong>#" + entrada.getId() + "</strong> "
                    + "a <strong>" + destino.getCorreoUsuario() + "</strong>." +
                "<br/><br/>" + detallePartido +
                "<br/><br/>A partir de este momento dejas de ser el titular y no podrás "
                    + "acceder al estadio con esta entrada. Si no fuiste tú, contacta a soporte de inmediato.");

            // Correo al destino
            notificar(destino,
                "Recibiste una entrada",
                "<strong>" + origen.getNombres() + " " + (origen.getApellidos() != null ? origen.getApellidos() : "")
                    + "</strong> (" + origen.getCorreoUsuario() + ") te transfirió la entrada "
                    + "<strong>#" + entrada.getId() + "</strong>." +
                "<br/><br/>" + detallePartido +
                "<br/><br/>Ya eres el nuevo titular. Encuentra los detalles en tu perfil dentro de Mundial 2026 Hub.");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Entrada transferida exitosamente. Se notificó por correo a ambas partes.",
                        "transferencia", guardada,
                        "idCorrelacion", correlacion
                    ));
        } catch (Exception e) {
            logger.severe("Error transfiriendo entrada: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // GET transferencias por entrada
    @GetMapping("/entrada/{idEntrada}")
    public ResponseEntity<?> getByEntrada(@PathVariable Long idEntrada) {
        return ResponseEntity.ok(transferenciaEntradaServiceAPI.findByIdEntrada(idEntrada));
    }

    // GET transferencias por usuario origen
    @GetMapping("/origen/{idUsuario}")
    public ResponseEntity<?> getByOrigen(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(transferenciaEntradaServiceAPI.findByIdUsuarioOrigen(idUsuario));
    }

    // GET todas (solo ADMIN — ver SecurityConfig)
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(transferenciaEntradaServiceAPI.getAll());
    }

    // ───────────────────────────────────────────────────────────────────────
    // Helpers
    // ───────────────────────────────────────────────────────────────────────

    private void registrarAuditoria(String correlacion, Long idUsuario, Long idTransferencia,
                                    String tipoEvento, String detalle, String estado,
                                    HttpServletRequest request) {
        try {
            EventoAuditoria ev = new EventoAuditoria();
            ev.setIdCorrelacion(correlacion);
            ev.setIdUsuario(idUsuario);
            ev.setTipoEvento(tipoEvento);
            ev.setModuloOrigen("TRANSFERENCIA");
            ev.setDetalle(detalle);
            ev.setTimestampEvento(new Date());
            ev.setEstadoResultado(estado);
            ev.setEntidadAfectada("TransferenciaEntrada");
            ev.setIdEntidad(idTransferencia);
            ev.setIpOrigen(Utilidad.obtenerIp(request));
            eventoAuditoriaServiceAPI.save(ev);
        } catch (Exception e) {
            logger.warning("No se pudo registrar auditoría transferencia: " + e.getMessage());
        }
    }

    private void notificar(Usuario u, String tipo, String mensajeHtml) {
        if (u == null || u.getCorreoUsuario() == null) {
			return;
		}
        try {
            emailService.enviarNotificacion(
                u.getCorreoUsuario(),
                u.getNombres() != null ? u.getNombres() : "Usuario",
                tipo, mensajeHtml
            );
        } catch (Exception e) {
            logger.warning("No se pudo enviar correo de transferencia a " + u.getCorreoUsuario()
                + ": " + e.getMessage());
        }
    }

    private String describirPartido(Partido p, Entrada e) {
        StringBuilder sb = new StringBuilder("<div style='background:#f4f6ff;border-left:4px solid #304ffe;"
            + "border-radius:0 8px 8px 0;padding:14px 18px;'>");
        if (p != null) {
            sb.append("<p style='margin:0 0 6px;color:#1a1a2e;font-size:14px;'><strong>")
              .append(p.getEquipoLocal() != null ? p.getEquipoLocal() : "—")
              .append(" vs ")
              .append(p.getEquipoVisitante() != null ? p.getEquipoVisitante() : "—")
              .append("</strong></p>");
            if (p.getFechaHora() != null) {
                java.text.SimpleDateFormat fmt =
                    new java.text.SimpleDateFormat("EEEE d 'de' MMMM yyyy, HH:mm 'h'",
                        new java.util.Locale("es", "CO"));
                sb.append("<p style='margin:0;color:#555;font-size:13px;'>")
                  .append(fmt.format(p.getFechaHora()))
                  .append("</p>");
            }
        }
        if (e.getTribuna() != null) {
            sb.append("<p style='margin:6px 0 0;color:#555;font-size:13px;'>Tribuna: <strong>")
              .append(e.getTribuna()).append("</strong></p>");
        }
        if (e.getPrecio() != null) {
            sb.append("<p style='margin:2px 0 0;color:#555;font-size:13px;'>Valor: $")
              .append(e.getPrecio()).append("</p>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}

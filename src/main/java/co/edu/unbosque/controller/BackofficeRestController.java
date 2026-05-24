package co.edu.unbosque.controller;

import java.util.Date;
import java.util.HashMap;
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

import co.edu.unbosque.entity.DiscrepanciaDatos;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.service.api.DiscrepanciaDatosServiceAPI;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.NotificacionServiceAPI;
import co.edu.unbosque.service.api.ReembolsoEntradaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/backoffice")
public class BackofficeRestController {

    private static final Logger logger = Logger.getLogger(BackofficeRestController.class.getName());

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    @Autowired
    private DiscrepanciaDatosServiceAPI discrepanciaDatosServiceAPI;

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private EntradaServiceAPI entradaServiceAPI;

    @Autowired
    private NotificacionServiceAPI notificacionServiceAPI;

    @Autowired
    private ReembolsoEntradaServiceAPI reembolsoEntradaServiceAPI;

    // HU-30: Panel de compliance - logs de seguridad
    @GetMapping("/compliance/logins-fallidos")
    public ResponseEntity<?> getLoginsFallidos() {
        logger.info("Consultando logins fallidos para compliance");
        List<EventoAuditoria> eventos = eventoAuditoriaServiceAPI.getAll();
        List<EventoAuditoria> fallidos = eventos.stream()
                .filter(e -> "LOGIN".equals(e.getTipoEvento())
                        && "ERROR".equals(e.getEstadoResultado()))
                .toList();
        return ResponseEntity.ok(Map.of(
            "loginsFallidos", fallidos,
            "total", fallidos.size()
        ));
    }

    // HU-30: Auditar por correlacion
    @GetMapping("/compliance/correlacion/{idCorrelacion}")
    public ResponseEntity<?> auditarPorCorrelacion(@PathVariable String idCorrelacion) {
        logger.info("Auditando por correlacion: " + idCorrelacion);
        List<EventoAuditoria> eventos = eventoAuditoriaServiceAPI
                .findByIdCorrelacion(idCorrelacion);
        if (eventos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay eventos con ese ID de correlacion."));
        }
        return ResponseEntity.ok(eventos);
    }

    // HU-31: Dashboard de métricas operativas
    @GetMapping("/metricas")
    public ResponseEntity<?> getMetricas() {
        logger.info("Consultando metricas operativas");
        Map<String, Object> metricas = new HashMap<>();

        // Conteos generales
        metricas.put("totalUsuarios", usuarioServiceAPI.getAll().size());
        metricas.put("totalNotificaciones", notificacionServiceAPI.getAll().size());
        metricas.put("totalEntradas", entradaServiceAPI.getAll().size());
        metricas.put("totalEventosAuditoria", eventoAuditoriaServiceAPI.getAll().size());

        // Entradas por estado
        metricas.put("entradasDisponibles", entradaServiceAPI.findByEstado("DISPONIBLE").size());
        metricas.put("entradasReservadas", entradaServiceAPI.findByEstado("RESERVADA").size());
        metricas.put("entradasPagadas", entradaServiceAPI.findByEstado("PAGADA").size());
        metricas.put("entradasExpiradas", entradaServiceAPI.findByEstado("EXPIRADA").size());
        metricas.put("entradasReembolsadas", entradaServiceAPI.findByEstado("REEMBOLSADA").size());

        // Reembolsos pendientes
        metricas.put("reembolsosPendientes", reembolsoEntradaServiceAPI.findByEstado("PENDIENTE").size());

        // Discrepancias sin resolver
        metricas.put("discrepanciasSinResolver", discrepanciaDatosServiceAPI.findByResuelto((byte) 0).size());

        return ResponseEntity.ok(metricas);
    }

    // HU-31: Registrar discrepancia de datos
    @PostMapping("/discrepancia")
    public ResponseEntity<?> registrarDiscrepancia(@RequestBody DiscrepanciaDatos discrepancia,
                                                   HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Registrando discrepancia de datos");
            discrepancia.setFechaDeteccion(new Date());
            discrepancia.setResuelto((byte) 0);
            DiscrepanciaDatos guardada = discrepanciaDatosServiceAPI.save(discrepancia);

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setTipoEvento("DISCREPANCIA_DATOS");
            evento.setModuloOrigen("BACKOFFICE");
            evento.setDetalle("Discrepancia en campo: " + discrepancia.getCampoDiscrepancia());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Discrepancia registrada.", "discrepancia", guardada));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-31: Resolver discrepancia
    @PutMapping("/discrepancia/resolver/{idDiscrepancia}")
    public ResponseEntity<?> resolverDiscrepancia(@PathVariable Long idDiscrepancia) {
        DiscrepanciaDatos discrepancia = discrepanciaDatosServiceAPI.get(idDiscrepancia);
        if (discrepancia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Discrepancia no encontrada."));
        }
        discrepancia.setResuelto((byte) 1);
        discrepanciaDatosServiceAPI.save(discrepancia);
        return ResponseEntity.ok(Map.of("mensaje", "Discrepancia resuelta."));
    }

    // GET todas las discrepancias
    @GetMapping("/discrepancias")
    public ResponseEntity<?> getDiscrepancias() {
        return ResponseEntity.ok(discrepanciaDatosServiceAPI.getAll());
    }

    // GET discrepancias sin resolver
    @GetMapping("/discrepancias/pendientes")
    public ResponseEntity<?> getDiscrepanciasPendientes() {
        List<DiscrepanciaDatos> pendientes = discrepanciaDatosServiceAPI.findByResuelto((byte) 0);
        if (pendientes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay discrepancias pendientes."));
        }
        return ResponseEntity.ok(pendientes);
    }

    // GET todos los eventos de auditoria
    @GetMapping("/auditoria")
    public ResponseEntity<?> getAuditoria() {
        return ResponseEntity.ok(eventoAuditoriaServiceAPI.getAll());
    }
}
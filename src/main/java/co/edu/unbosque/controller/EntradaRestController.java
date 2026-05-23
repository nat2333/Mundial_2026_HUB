package co.edu.unbosque.controller;

import java.util.Calendar;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.FacturaEntradaServiceAPI;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/entrada")
public class EntradaRestController {

    private static final Logger logger = Logger.getLogger(EntradaRestController.class.getName());

    @Autowired
    private EntradaServiceAPI entradaServiceAPI;

    @Autowired
    private FacturaEntradaServiceAPI facturaEntradaServiceAPI;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    // HU-12: Reservar entrada
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarEntrada(@RequestBody Entrada entrada,
                                             HttpServletRequest request) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Reservando entrada para partido: " + entrada.getIdPartido());

            // Verificar disponibilidad
            List<Entrada> disponibles = entradaServiceAPI
                    .findByIdPartidoAndEstado(entrada.getIdPartido(), "DISPONIBLE");
            if (disponibles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No hay entradas disponibles para este partido."));
            }

            // TTL de 15 minutos
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 15);
            Date expiracion = cal.getTime();

            entrada.setEstado("RESERVADA");
            entrada.setFechaReserva(new Date());
            entrada.setFechaExpiracionReserva(expiracion);
            entrada.setIdCorrelacion(correlacion);
            Entrada guardada = entradaServiceAPI.save(entrada);

            registrarAuditoria(correlacion, guardada.getIdTitular(), guardada.getId(),
                "ENTRADA_RESERVADA",
                "Entrada " + guardada.getId() + " reservada | partido: "
                    + guardada.getIdPartido() + " | tribuna: " + guardada.getTribuna(), "OK", request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Entrada reservada. Tienes 15 minutos para confirmar el pago.",
                        "entrada", guardada,
                        "expiracion", expiracion
                    ));
        } catch (Exception e) {
            logger.severe("Error reservando entrada: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-13: Confirmar pago (Stripe sandbox)
    @PutMapping("/confirmarPago/{idEntrada}")
    public ResponseEntity<?> confirmarPago(
            @PathVariable Long idEntrada,
            @RequestParam String idTransaccion) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Confirmando pago entrada: " + idEntrada);

            Entrada entrada = entradaServiceAPI.get(idEntrada);
            if (entrada == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Entrada no encontrada."));
            }

            if (!"RESERVADA".equals(entrada.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "La entrada no está en estado RESERVADA."));
            }

            // Verificar que no expiró
            if (entrada.getFechaExpiracionReserva() != null
                    && entrada.getFechaExpiracionReserva().before(new Date())) {
                entrada.setEstado("EXPIRADA");
                entradaServiceAPI.save(entrada);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "La reserva ha expirado."));
            }

            entrada.setEstado("PAGADA");
            entrada.setFechaPago(new Date());
            entrada.setIdTransaccionPago(idTransaccion);
            entradaServiceAPI.save(entrada);

            // Generar factura automáticamente al confirmar pago
            try {
                facturaEntradaServiceAPI.generarFactura(entrada.getId());
                logger.info("[" + correlacion + "] Factura generada para entrada: " + entrada.getId());
            } catch (Exception ef) {
                logger.warning("[" + correlacion + "] Pago confirmado pero error al generar factura: " + ef.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                "mensaje", "Pago confirmado exitosamente. Factura generada.",
                "entrada", entrada,
                "idTransaccion", idTransaccion
            ));
        } catch (Exception e) {
            logger.severe("Error confirmando pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // Verificar entradas expiradas
    @PutMapping("/verificarExpiracion")
    public ResponseEntity<?> verificarExpiracion() {
        logger.info("Verificando entradas expiradas");
        List<Entrada> reservadas = entradaServiceAPI.findByEstado("RESERVADA");
        int expiradas = 0;
        for (Entrada entrada : reservadas) {
            if (entrada.getFechaExpiracionReserva() != null
                    && entrada.getFechaExpiracionReserva().before(new Date())) {
                entrada.setEstado("EXPIRADA");
                entradaServiceAPI.save(entrada);
                expiradas++;
            }
        }
        return ResponseEntity.ok(Map.of(
            "mensaje", "Verificacion completada.",
            "entradasExpiradas", expiradas
        ));
    }

    // POST crear entrada disponible
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Entrada entrada) {
        entrada.setEstado("DISPONIBLE");
        Entrada guardada = entradaServiceAPI.save(entrada);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // GET todas
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(entradaServiceAPI.getAll());
    }

    // GET por partido
    @GetMapping("/partido/{idPartido}")
    public ResponseEntity<?> getByPartido(@PathVariable Long idPartido) {
        List<Entrada> entradas = entradaServiceAPI.findByIdPartido(idPartido);
        if (entradas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay entradas para este partido."));
        }
        return ResponseEntity.ok(entradas);
    }

    // GET por titular — libera reservas vencidas antes de devolver
    @GetMapping("/titular/{idUsuario}")
    public ResponseEntity<?> getByTitular(@PathVariable Long idUsuario) {
        List<Entrada> entradas = entradaServiceAPI.findByIdTitular(idUsuario);
        Date now = new Date();
        java.util.Iterator<Entrada> it = entradas.iterator();
        while (it.hasNext()) {
            Entrada e = it.next();
            if ("RESERVADA".equals(e.getEstado())
                    && e.getFechaExpiracionReserva() != null
                    && e.getFechaExpiracionReserva().before(now)) {
                // Devolver al pool: estado DISPONIBLE y limpiar titular/fechas
                e.setEstado("DISPONIBLE");
                e.setIdTitular(null);
                e.setFechaReserva(null);
                e.setFechaExpiracionReserva(null);
                entradaServiceAPI.save(e);
                it.remove();
            }
        }
        if (entradas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay entradas para este usuario."));
        }
        return ResponseEntity.ok(entradas);
    }

    // GET por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> getByEstado(@PathVariable String estado) {
        List<Entrada> entradas = entradaServiceAPI.findByEstado(estado);
        if (entradas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay entradas con estado " + estado));
        }
        return ResponseEntity.ok(entradas);
    }

    // GET por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Entrada entrada = entradaServiceAPI.get(id);
        if (entrada == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Entrada no encontrada."));
        }
        return ResponseEntity.ok(entrada);
    }

    private void registrarAuditoria(String idCorrelacion, Long idUsuario, Long idEntidad,
                                     String tipoEvento, String detalle, String resultado,
                                     HttpServletRequest request) {
        try {
            EventoAuditoria ev = new EventoAuditoria();
            ev.setIdCorrelacion(idCorrelacion);
            ev.setIdUsuario(idUsuario);
            ev.setTipoEvento(tipoEvento);
            ev.setModuloOrigen("ENTRADAS");
            ev.setDetalle(detalle);
            ev.setEntidadAfectada("entrada");
            ev.setIdEntidad(idEntidad);
            ev.setTimestampEvento(new Date());
            ev.setEstadoResultado(resultado);
            ev.setIpOrigen(Utilidad.obtenerIp(request));
            eventoAuditoriaServiceAPI.save(ev);
        } catch (Exception e) {
            logger.warning("No se pudo registrar auditoría [" + tipoEvento + "]: " + e.getMessage());
        }
    }
}
package co.edu.unbosque.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.IntercambioLamina;
import co.edu.unbosque.entity.LaminaUsuario;
import co.edu.unbosque.service.api.IntercambioLaminaServiceAPI;
import co.edu.unbosque.service.api.LaminaUsuarioServiceAPI;
import co.edu.unbosque.utils.Utilidad;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/intercambio")
public class IntercambioLaminaRestController {

    private static final Logger logger = Logger.getLogger(IntercambioLaminaRestController.class.getName());

    @Autowired
    private IntercambioLaminaServiceAPI intercambioLaminaServiceAPI;

    @Autowired
    private LaminaUsuarioServiceAPI laminaUsuarioServiceAPI;

    // HU-22: Solicitar intercambio
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarIntercambio(@RequestBody IntercambioLamina intercambio) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Solicitud de intercambio");

            // Verificar que el usuario oferta tiene la lamina
            Optional<LaminaUsuario> laminaOferta = laminaUsuarioServiceAPI
                    .findByIdUsuarioAndIdLamina(
                        intercambio.getIdUsuarioOferta(),
                        intercambio.getIdLaminaOferta());
            if (laminaOferta.isEmpty() || laminaOferta.get().getCantidad() <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "No tienes esta lamina disponible para intercambio."));
            }

            // Verificar que el receptor tiene la lamina que ofrece
            Optional<LaminaUsuario> laminaReceptor = laminaUsuarioServiceAPI
                    .findByIdUsuarioAndIdLamina(
                        intercambio.getIdUsuarioReceptor(),
                        intercambio.getIdLaminaReceptor());
            if (laminaReceptor.isEmpty() || laminaReceptor.get().getCantidad() <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El receptor no tiene la lamina disponible para intercambio."));
            }

            intercambio.setEstado("PENDIENTE");
            intercambio.setFechaSolicitud(new Date());
            intercambio.setIdCorrelacion(correlacion);
            IntercambioLamina guardado = intercambioLaminaServiceAPI.save(intercambio);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Solicitud de intercambio enviada.",
                        "intercambio", guardado,
                        "idCorrelacion", correlacion
                    ));
        } catch (Exception e) {
            logger.severe("Error en solicitud de intercambio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-22: Aceptar intercambio
    @PutMapping("/aceptar/{idIntercambio}")
    public ResponseEntity<?> aceptarIntercambio(@PathVariable Long idIntercambio) {
        try {
            IntercambioLamina intercambio = intercambioLaminaServiceAPI.get(idIntercambio);
            if (intercambio == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Intercambio no encontrado."));
            }

            if (!"PENDIENTE".equals(intercambio.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El intercambio ya fue procesado."));
            }

            // Realizar el intercambio
            // Reducir cantidad al usuario oferta
            Optional<LaminaUsuario> laminaOferta = laminaUsuarioServiceAPI
                    .findByIdUsuarioAndIdLamina(
                        intercambio.getIdUsuarioOferta(),
                        intercambio.getIdLaminaOferta());
            if (laminaOferta.isPresent()) {
                LaminaUsuario lu = laminaOferta.get();
                lu.setCantidad(lu.getCantidad() - 1);
                laminaUsuarioServiceAPI.save(lu);
            }

            // Agregar lamina al receptor
            Optional<LaminaUsuario> laminaReceptorNueva = laminaUsuarioServiceAPI
                    .findByIdUsuarioAndIdLamina(
                        intercambio.getIdUsuarioReceptor(),
                        intercambio.getIdLaminaOferta());
            if (laminaReceptorNueva.isPresent()) {
                LaminaUsuario lu = laminaReceptorNueva.get();
                lu.setCantidad(lu.getCantidad() + 1);
                laminaUsuarioServiceAPI.save(lu);
            } else {
                LaminaUsuario nueva = new LaminaUsuario();
                nueva.setIdUsuario(intercambio.getIdUsuarioReceptor());
                nueva.setIdLamina(intercambio.getIdLaminaOferta());
                nueva.setCantidad(1);
                nueva.setEnIntercambio((byte) 0);
                nueva.setFechaObtencion(new Date());
                laminaUsuarioServiceAPI.save(nueva);
            }

            // Reducir cantidad al receptor
            Optional<LaminaUsuario> laminaReceptor = laminaUsuarioServiceAPI
                    .findByIdUsuarioAndIdLamina(
                        intercambio.getIdUsuarioReceptor(),
                        intercambio.getIdLaminaReceptor());
            if (laminaReceptor.isPresent()) {
                LaminaUsuario lu = laminaReceptor.get();
                lu.setCantidad(lu.getCantidad() - 1);
                laminaUsuarioServiceAPI.save(lu);
            }

            // Agregar lamina al ofertante
            Optional<LaminaUsuario> laminaOfertaNueva = laminaUsuarioServiceAPI
                    .findByIdUsuarioAndIdLamina(
                        intercambio.getIdUsuarioOferta(),
                        intercambio.getIdLaminaReceptor());
            if (laminaOfertaNueva.isPresent()) {
                LaminaUsuario lu = laminaOfertaNueva.get();
                lu.setCantidad(lu.getCantidad() + 1);
                laminaUsuarioServiceAPI.save(lu);
            } else {
                LaminaUsuario nueva = new LaminaUsuario();
                nueva.setIdUsuario(intercambio.getIdUsuarioOferta());
                nueva.setIdLamina(intercambio.getIdLaminaReceptor());
                nueva.setCantidad(1);
                nueva.setEnIntercambio((byte) 0);
                nueva.setFechaObtencion(new Date());
                laminaUsuarioServiceAPI.save(nueva);
            }

            intercambio.setEstado("ACEPTADO");
            intercambio.setFechaResolucion(new Date());
            intercambioLaminaServiceAPI.save(intercambio);

            return ResponseEntity.ok(Map.of("mensaje", "Intercambio aceptado exitosamente."));
        } catch (Exception e) {
            logger.severe("Error aceptando intercambio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-22: Rechazar intercambio
    @PutMapping("/rechazar/{idIntercambio}")
    public ResponseEntity<?> rechazarIntercambio(@PathVariable Long idIntercambio) {
        IntercambioLamina intercambio = intercambioLaminaServiceAPI.get(idIntercambio);
        if (intercambio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Intercambio no encontrado."));
        }
        intercambio.setEstado("RECHAZADO");
        intercambio.setFechaResolucion(new Date());
        intercambioLaminaServiceAPI.save(intercambio);
        return ResponseEntity.ok(Map.of("mensaje", "Intercambio rechazado."));
    }

    // GET intercambios pendientes del usuario
    @GetMapping("/pendientes/{idUsuario}")
    public ResponseEntity<?> getPendientes(@PathVariable Long idUsuario) {
        List<IntercambioLamina> pendientes = intercambioLaminaServiceAPI
                .findByIdUsuarioReceptor(idUsuario);
        pendientes.removeIf(i -> !"PENDIENTE".equals(i.getEstado()));
        if (pendientes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No tienes intercambios pendientes."));
        }
        return ResponseEntity.ok(pendientes);
    }

    // GET todos los intercambios
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(intercambioLaminaServiceAPI.getAll());
    }
}
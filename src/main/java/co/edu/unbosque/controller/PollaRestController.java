package co.edu.unbosque.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import co.edu.unbosque.entity.MiembroPolla;
import co.edu.unbosque.entity.Polla;
import co.edu.unbosque.service.api.MiembroPollaServiceAPI;
import co.edu.unbosque.service.api.PollaServiceAPI;

@RestController
@RequestMapping("/polla")
public class PollaRestController {

    private static final Logger logger = Logger.getLogger(PollaRestController.class.getName());

    @Autowired
    private PollaServiceAPI pollaServiceAPI;

    @Autowired
    private MiembroPollaServiceAPI miembroPollaServiceAPI;

    // HU-16: Crear una polla
    @PostMapping("/crear")
    public ResponseEntity<?> crearPolla(@RequestBody Polla polla) {
        try {
            logger.info("Creando polla: " + polla.getNombre());
            String codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            polla.setCodigoInvitacion(codigo);
            polla.setEstado("ACTIVA");
            polla.setFechaCreacion(new Date());
            Polla guardada = pollaServiceAPI.save(polla);

            // El creador se une automáticamente
            MiembroPolla miembro = new MiembroPolla();
            miembro.setIdPolla(guardada.getId());
            miembro.setIdUsuario(guardada.getIdCreador());
            miembro.setPuntajeTotal(0);
            miembro.setPosicionRanking(1);
            miembro.setFechaUnion(new Date());
            miembroPollaServiceAPI.save(miembro);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Polla creada exitosamente.",
                        "polla", guardada,
                        "codigoInvitacion", codigo
                    ));
        } catch (Exception e) {
            logger.severe("Error creando polla: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-17: Unirse a una polla
    @PostMapping("/unirse/{codigo}/{idUsuario}")
    public ResponseEntity<?> unirseAPolla(
            @PathVariable String codigo,
            @PathVariable Long idUsuario) {
        try {
            logger.info("Usuario " + idUsuario + " uniéndose a polla con código: " + codigo);
            Optional<Polla> pollaOpt = pollaServiceAPI.findByCodigoInvitacion(codigo);
            if (pollaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Código de invitación inválido."));
            }

            Polla polla = pollaOpt.get();

            if ("CERRADA".equals(polla.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "La polla está cerrada."));
            }

            Optional<MiembroPolla> yaExiste = miembroPollaServiceAPI
                    .findByIdPollaAndIdUsuario(polla.getId(), idUsuario);
            if (yaExiste.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("mensaje", "Ya eres miembro de esta polla."));
            }

            MiembroPolla miembro = new MiembroPolla();
            miembro.setIdPolla(polla.getId());
            miembro.setIdUsuario(idUsuario);
            miembro.setPuntajeTotal(0);
            miembro.setFechaUnion(new Date());
            miembroPollaServiceAPI.save(miembro);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Te has unido a la polla " + polla.getNombre() + " exitosamente."));
        } catch (Exception e) {
            logger.severe("Error uniéndose a polla: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-19: Ver ranking de la polla
    @GetMapping("/ranking/{idPolla}")
    public ResponseEntity<?> getRanking(@PathVariable Long idPolla) {
        logger.info("Consultando ranking de polla: " + idPolla);
        List<MiembroPolla> ranking = miembroPollaServiceAPI.findRankingByIdPolla(idPolla);
        if (ranking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay miembros en esta polla."));
        }
        return ResponseEntity.ok(ranking);
    }

    // GET todas las pollas
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(pollaServiceAPI.getAll());
    }

    // GET polla por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Polla polla = pollaServiceAPI.get(id);
        if (polla == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Polla no encontrada."));
        }
        return ResponseEntity.ok(polla);
    }

    // GET pollas por usuario creador
    @GetMapping("/creador/{idUsuario}")
    public ResponseEntity<?> getByCreador(@PathVariable Long idUsuario) {
        List<Polla> pollas = pollaServiceAPI.findByIdCreador(idUsuario);
        if (pollas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay pollas creadas por este usuario."));
        }
        return ResponseEntity.ok(pollas);
    }

    // GET miembros de una polla
    @GetMapping("/miembros/{idPolla}")
    public ResponseEntity<?> getMiembros(@PathVariable Long idPolla) {
        List<MiembroPolla> miembros = miembroPollaServiceAPI.findByIdPolla(idPolla);
        if (miembros.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay miembros en esta polla."));
        }
        return ResponseEntity.ok(miembros);
    }

    // GET pollas donde el usuario es miembro
    @GetMapping("/miembro/{idUsuario}")
    public ResponseEntity<?> getByMiembro(@PathVariable Long idUsuario) {
        List<MiembroPolla> miembros = miembroPollaServiceAPI.findByIdUsuario(idUsuario);
        if (miembros.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No eres miembro de ninguna polla."));
        }
        List<Polla> pollas = miembros.stream()
                .map(m -> pollaServiceAPI.get(m.getIdPolla()))
                .filter(p -> p != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pollas);
    }

    // PUT cerrar polla
    @PutMapping("/cerrar/{idPolla}")
    public ResponseEntity<?> cerrarPolla(@PathVariable Long idPolla) {
        Polla polla = pollaServiceAPI.get(idPolla);
        if (polla == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Polla no encontrada."));
        }
        polla.setEstado("CERRADA");
        pollaServiceAPI.save(polla);
        return ResponseEntity.ok(Map.of("mensaje", "Polla cerrada exitosamente."));
    }
}
package co.edu.unbosque.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.MiembroPolla;
import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.entity.Pronostico;
import co.edu.unbosque.service.api.MiembroPollaServiceAPI;
import co.edu.unbosque.service.api.PartidoServiceAPI;
import co.edu.unbosque.service.api.PronosticoServiceAPI;

@RestController
@RequestMapping("/pronostico")
public class PronosticoRestController {

    private static final Logger logger = Logger.getLogger(PronosticoRestController.class.getName());

    @Autowired
    private PronosticoServiceAPI pronosticoServiceAPI;

    @Autowired
    private MiembroPollaServiceAPI miembroPollaServiceAPI;

    @Autowired
    private PartidoServiceAPI partidoServiceAPI;

    // HU-18: Registrar pronóstico
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPronostico(@RequestBody Pronostico pronostico) {
        try {
            logger.info("Registrando pronostico usuario: " + pronostico.getIdUsuario());

            // Verificar que el usuario es miembro de la polla
            Optional<MiembroPolla> miembro = miembroPollaServiceAPI
                    .findByIdPollaAndIdUsuario(
                        pronostico.getIdPolla(),
                        pronostico.getIdUsuario());
            if (miembro.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No eres miembro de esta polla."));
            }

            // Verificar que el partido no ha iniciado
            Partido partido = partidoServiceAPI.get(pronostico.getIdPartido());
            if (partido == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Partido no encontrado."));
            }

            if ("EN_JUEGO".equals(partido.getEstado()) ||
                "FINALIZADO".equals(partido.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "No puedes registrar un pronostico para un partido que ya inicio o finalizo."));
            }

            // Verificar si ya existe un pronóstico
            Optional<Pronostico> existente = pronosticoServiceAPI
                    .findByIdPollaAndIdUsuarioAndIdPartido(
                        pronostico.getIdPolla(),
                        pronostico.getIdUsuario(),
                        pronostico.getIdPartido());

            if (existente.isPresent()) {
                if (existente.get().getCerrado() == 1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("mensaje", "El pronostico ya esta cerrado y no puede modificarse."));
                }
                // Actualizar pronóstico existente
                Pronostico actual = existente.get();
                actual.setResultadoPredicho(pronostico.getResultadoPredicho());
                actual.setGolesLocalPredichos(pronostico.getGolesLocalPredichos());
                actual.setGolesVisitantePredichos(pronostico.getGolesVisitantePredichos());
                actual.setFechaRegistro(new Date());
                Pronostico actualizado = pronosticoServiceAPI.save(actual);
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Pronostico actualizado exitosamente.",
                    "pronostico", actualizado));
            }

            pronostico.setFechaRegistro(new Date());
            pronostico.setPuntajeObtenido(0);
            pronostico.setCerrado((byte) 0);
            Pronostico guardado = pronosticoServiceAPI.save(pronostico);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Pronostico registrado exitosamente.",
                        "pronostico", guardado));

        } catch (Exception e) {
            logger.severe("Error registrando pronostico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-19: Calcular puntajes al finalizar partido
    @PostMapping("/calcularPuntajes/{idPartido}")
    public ResponseEntity<?> calcularPuntajes(@PathVariable Long idPartido) {
        try {
            logger.info("Calculando puntajes para partido: " + idPartido);
            Partido partido = partidoServiceAPI.get(idPartido);
            if (partido == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Partido no encontrado."));
            }

            if (!"FINALIZADO".equals(partido.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El partido aun no ha finalizado."));
            }

            List<Pronostico> pronosticos = pronosticoServiceAPI.findByIdPartido(idPartido);
            int calculados = 0;

            for (Pronostico p : pronosticos) {
                int puntaje = 0;

                // Calcular resultado real
                String resultadoReal;
                if (partido.getGolesLocal() > partido.getGolesVisitante()) {
                    resultadoReal = "LOCAL";
                } else if (partido.getGolesLocal() < partido.getGolesVisitante()) {
                    resultadoReal = "VISITANTE";
                } else {
                    resultadoReal = "EMPATE";
                }

                // 3 puntos por acertar resultado
                if (resultadoReal.equals(p.getResultadoPredicho())) {
                    puntaje += 3;
                    // 2 puntos bonus por marcador exacto
                    if (p.getGolesLocalPredichos() != null &&
                        p.getGolesVisitantePredichos() != null &&
                        p.getGolesLocalPredichos().equals(partido.getGolesLocal()) &&
                        p.getGolesVisitantePredichos().equals(partido.getGolesVisitante())) {
                        puntaje += 2;
                    }
                }

                p.setPuntajeObtenido(puntaje);
                p.setCerrado((byte) 1);
                pronosticoServiceAPI.save(p);

                // Actualizar puntaje del miembro en la polla
                Optional<MiembroPolla> miembroOpt = miembroPollaServiceAPI
                        .findByIdPollaAndIdUsuario(p.getIdPolla(), p.getIdUsuario());
                if (miembroOpt.isPresent()) {
                    MiembroPolla miembro = miembroOpt.get();
                    miembro.setPuntajeTotal(
                        (miembro.getPuntajeTotal() == null ? 0 : miembro.getPuntajeTotal()) + puntaje);
                    miembroPollaServiceAPI.save(miembro);
                }
                calculados++;
            }

            return ResponseEntity.ok(Map.of(
                "mensaje", "Puntajes calculados exitosamente.",
                "pronosticosCalculados", calculados
            ));

        } catch (Exception e) {
            logger.severe("Error calculando puntajes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // GET pronosticos por polla
    @GetMapping("/polla/{idPolla}")
    public ResponseEntity<?> getByPolla(@PathVariable Long idPolla) {
        List<Pronostico> pronosticos = pronosticoServiceAPI.findByIdPolla(idPolla);
        if (pronosticos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay pronosticos para esta polla."));
        }
        return ResponseEntity.ok(pronosticos);
    }

    // GET pronosticos por usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getByUsuario(@PathVariable Long idUsuario) {
        List<Pronostico> pronosticos = pronosticoServiceAPI.findByIdUsuario(idUsuario);
        if (pronosticos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay pronosticos para este usuario."));
        }
        return ResponseEntity.ok(pronosticos);
    }

    // GET todos
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(pronosticoServiceAPI.getAll());
    }
}
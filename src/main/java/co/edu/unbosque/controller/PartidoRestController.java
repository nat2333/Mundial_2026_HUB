package co.edu.unbosque.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.service.api.PartidoServiceAPI;
import co.edu.unbosque.utils.ApiDeportesAdapter;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/partido")
public class PartidoRestController {

    private static final Logger logger = Logger.getLogger(PartidoRestController.class.getName());

    @Autowired
    private PartidoServiceAPI partidoServiceAPI;

    @Autowired
    private ApiDeportesAdapter apiDeportesAdapter;

    // HU-05: Ver todos los partidos
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        logger.info("Consultando todos los partidos");
        return ResponseEntity.ok(partidoServiceAPI.getAll());
    }

    // HU-05: Ver proximos partidos programados
    @GetMapping("/proximos")
    public ResponseEntity<?> getProximos() {
        logger.info("Consultando partidos programados");
        List<Partido> partidos = partidoServiceAPI.findByEstado("PROGRAMADO");
        if (partidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos programados."));
        }
        return ResponseEntity.ok(partidos);
    }

    // HU-07: Ver resultados finalizados
    @GetMapping("/resultados")
    public ResponseEntity<?> getResultados() {
        logger.info("Consultando partidos finalizados");
        List<Partido> partidos = partidoServiceAPI.findByEstado("FINALIZADO");
        if (partidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos finalizados."));
        }
        return ResponseEntity.ok(partidos);
    }

    // HU-07: Ver partido en juego
    @GetMapping("/enJuego")
    public ResponseEntity<?> getEnJuego() {
        logger.info("Consultando partidos en juego");
        List<Partido> partidos = partidoServiceAPI.findByEstado("EN_JUEGO");
        if (partidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos en juego."));
        }
        return ResponseEntity.ok(partidos);
    }

    // HU-06: Filtrar por ciudad
    @GetMapping("/ciudad/{ciudad}")
    public ResponseEntity<?> getByCiudad(@PathVariable String ciudad) {
        logger.info("Consultando partidos en ciudad: " + ciudad);
        List<Partido> partidos = partidoServiceAPI.findByCiudad(ciudad);
        if (partidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos en " + ciudad));
        }
        return ResponseEntity.ok(partidos);
    }

    // HU-06: Filtrar por estadio
    @GetMapping("/estadio/{estadio}")
    public ResponseEntity<?> getByEstadio(@PathVariable String estadio) {
        logger.info("Consultando partidos en estadio: " + estadio);
        List<Partido> partidos = partidoServiceAPI.findByEstadio(estadio);
        if (partidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos en " + estadio));
        }
        return ResponseEntity.ok(partidos);
    }

    // HU-05: Filtrar por equipo
    @GetMapping("/equipo/{equipo}")
    public ResponseEntity<?> getByEquipo(@PathVariable String equipo) {
        logger.info("Consultando partidos del equipo: " + equipo);
        List<Partido> partidos = partidoServiceAPI.findByEquipo(equipo);
        if (partidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay partidos para " + equipo));
        }
        return ResponseEntity.ok(partidos);
    }

    // GET por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        logger.info("Consultando partido con ID: " + id);
        Partido partido = partidoServiceAPI.get(id);
        if (partido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Partido no encontrado."));
        }
        return ResponseEntity.ok(partido);
    }

    // POST crear partido
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Partido partido) {
        logger.info("Creando partido: " + partido.getEquipoLocal() + " vs " + partido.getEquipoVisitante());
        Partido guardado = partidoServiceAPI.save(partido);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // PUT actualizar partido
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Partido partido) {
        logger.info("Actualizando partido con ID: " + id);
        Partido existente = partidoServiceAPI.get(id);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Partido no encontrado."));
        }
        partido.setId(id);
        return ResponseEntity.ok(partidoServiceAPI.save(partido));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        logger.info("Eliminando partido con ID: " + id);
        Partido partido = partidoServiceAPI.get(id);
        if (partido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Partido no encontrado."));
        }
        partidoServiceAPI.delete(id);
        return ResponseEntity.ok(Map.of("mensaje", "Partido eliminado."));
    }

    // HU-25: Sincronizar desde API externa
    @GetMapping("/sincronizar")
    public ResponseEntity<?> sincronizarDesdeApi() {
        logger.info("Iniciando sincronizacion con API externa");
        List<Partido> partidos = apiDeportesAdapter.sincronizarPartidos();
        return ResponseEntity.ok(Map.of(
            "mensaje", "Sincronizacion completada.",
            "total", partidos.size(),
            "partidos", partidos
        ));
    }

    // DEBUG TEMPORAL: ver respuesta cruda de la API externa
    @GetMapping("/debug-api")
    public ResponseEntity<?> debugApi() {
        String raw = apiDeportesAdapter.getRawApiResponse();
        return ResponseEntity.ok(Map.of("raw", raw));
    }
}
package co.edu.unbosque.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.dto.SedeDTO;
import co.edu.unbosque.entity.Sede;
import co.edu.unbosque.service.api.SedeServiceAPI;

@RestController
@RequestMapping("/sedes")
public class SedeRestController {

    private static final Logger logger = Logger.getLogger(SedeRestController.class.getName());

    @Autowired
    private SedeServiceAPI sedeServiceAPI;

    // GET /sedes — listar todas las sedes
    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.info("Consultando todas las sedes del Mundial 2026");
        List<Sede> sedes = sedeServiceAPI.getAll();
        if (sedes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay sedes registradas."));
        }
        return ResponseEntity.ok(sedes);
    }

    // GET /sedes/{id} — sede por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        logger.info("Consultando sede con ID: " + id);
        Sede sede = sedeServiceAPI.get(id);
        if (sede == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Sede no encontrada."));
        }
        return ResponseEntity.ok(sede);
    }

    // GET /sedes/pais/{pais} — sedes filtradas por país
    @GetMapping("/pais/{pais}")
    public ResponseEntity<?> getByPais(@PathVariable String pais) {
        logger.info("Consultando sedes del país: " + pais);
        List<Sede> sedes = sedeServiceAPI.findByPais(pais);
        if (sedes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No se encontraron sedes en " + pais + "."));
        }
        return ResponseEntity.ok(sedes);
    }

    // GET /sedes/mapa — datos de coordenadas para vista de mapa
    @GetMapping("/mapa")
    public ResponseEntity<?> getMapa() {
        logger.info("Consultando sedes para vista de mapa");
        List<Sede> sedes = sedeServiceAPI.getAll();
        if (sedes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay sedes disponibles."));
        }
        List<SedeDTO> marcadores = sedes.stream()
                .map(s -> new SedeDTO(s.getId(), s.getNombreEstadio(), s.getCiudad(),
                        s.getPais(), s.getLatitud(), s.getLongitud()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(marcadores);
    }
}

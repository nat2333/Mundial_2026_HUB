package co.edu.unbosque.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Lamina;
import co.edu.unbosque.entity.LaminaUsuario;
import co.edu.unbosque.entity.PaqueteLaminas;
import co.edu.unbosque.service.api.LaminaServiceAPI;
import co.edu.unbosque.service.api.LaminaUsuarioServiceAPI;
import co.edu.unbosque.service.api.PaqueteLaminasServiceAPI;

@RestController
@RequestMapping("/lamina")
public class LaminaRestController {

    private static final Logger logger = Logger.getLogger(LaminaRestController.class.getName());

    @Autowired
    private LaminaServiceAPI laminaServiceAPI;

    @Autowired
    private LaminaUsuarioServiceAPI laminaUsuarioServiceAPI;

    @Autowired
    private PaqueteLaminasServiceAPI paqueteLaminasServiceAPI;

    // HU-20: Abrir paquete de láminas
    @PostMapping("/abrirPaquete/{idUsuario}")
    public ResponseEntity<?> abrirPaquete(
            @PathVariable Long idUsuario,
            @RequestParam(defaultValue = "LOGIN_DIARIO") String tipoOrigen,
            @RequestParam(required = false) String codigoPromo) {
        try {
            logger.info("Abriendo paquete para usuario: " + idUsuario);

            List<Lamina> todasLaminas = laminaServiceAPI.getAll();
            if (todasLaminas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No hay laminas disponibles en el sistema."));
            }

            // Registrar el paquete
            PaqueteLaminas paquete = new PaqueteLaminas();
            paquete.setIdUsuario(idUsuario);
            paquete.setTipoOrigen(tipoOrigen);
            paquete.setCodigoPromo(codigoPromo);
            paquete.setFechaApertura(new Date());
            paquete.setAbierto((byte) 1);
            paqueteLaminasServiceAPI.save(paquete);

            // Seleccionar 5 láminas aleatorias
            Random random = new Random();
            List<Lamina> laminasObtenidas = new ArrayList<>();
            List<LaminaUsuario> nuevas = new ArrayList<>();
            List<LaminaUsuario> repetidas = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Lamina laminaAleatoria = todasLaminas.get(
                    random.nextInt(todasLaminas.size()));
                laminasObtenidas.add(laminaAleatoria);

                Optional<LaminaUsuario> existente = laminaUsuarioServiceAPI
                        .findByIdUsuarioAndIdLamina(idUsuario, laminaAleatoria.getId());

                if (existente.isPresent()) {
                    LaminaUsuario lu = existente.get();
                    lu.setCantidad(lu.getCantidad() + 1);
                    laminaUsuarioServiceAPI.save(lu);
                    repetidas.add(lu);
                } else {
                    LaminaUsuario nueva = new LaminaUsuario();
                    nueva.setIdUsuario(idUsuario);
                    nueva.setIdLamina(laminaAleatoria.getId());
                    nueva.setCantidad(1);
                    nueva.setEnIntercambio((byte) 0);
                    nueva.setFechaObtencion(new Date());
                    laminaUsuarioServiceAPI.save(nueva);
                    nuevas.add(nueva);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Paquete abierto exitosamente.",
                        "laminasObtenidas", laminasObtenidas,
                        "nuevas", nuevas.size(),
                        "repetidas", repetidas.size()
                    ));
        } catch (Exception e) {
            logger.severe("Error abriendo paquete: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-21: Ver álbum del usuario
    @GetMapping("/album/{idUsuario}")
    public ResponseEntity<?> verAlbum(@PathVariable Long idUsuario) {
        logger.info("Consultando album del usuario: " + idUsuario);
        List<LaminaUsuario> album = laminaUsuarioServiceAPI.findByIdUsuario(idUsuario);
        if (album.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El usuario no tiene laminas aun."));
        }
        List<Lamina> todasLaminas = laminaServiceAPI.getAll();
        return ResponseEntity.ok(Map.of(
            "album", album,
            "totalLaminas", todasLaminas.size(),
            "laminasObtenidas", album.size(),
            "porcentaje", Math.round((album.size() * 100.0) / todasLaminas.size())
        ));
    }

    // HU-21: Ver láminas repetidas
    @GetMapping("/repetidas/{idUsuario}")
    public ResponseEntity<?> verRepetidas(@PathVariable Long idUsuario) {
        logger.info("Consultando laminas repetidas del usuario: " + idUsuario);
        List<LaminaUsuario> todasLaminas = laminaUsuarioServiceAPI.findByIdUsuario(idUsuario);
        List<LaminaUsuario> repetidas = new ArrayList<>();
        for (LaminaUsuario lu : todasLaminas) {
            if (lu.getCantidad() > 1) {
                repetidas.add(lu);
            }
        }
        if (repetidas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No tienes laminas repetidas."));
        }
        return ResponseEntity.ok(repetidas);
    }

    // Poner lámina en intercambio
    @PutMapping("/enIntercambio/{idLaminaUsuario}")
    public ResponseEntity<?> ponerEnIntercambio(@PathVariable Long idLaminaUsuario) {
        LaminaUsuario lu = laminaUsuarioServiceAPI.get(idLaminaUsuario);
        if (lu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Lamina no encontrada."));
        }
        if (lu.getCantidad() <= 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "No puedes poner en intercambio tu unica copia."));
        }
        lu.setEnIntercambio((byte) 1);
        laminaUsuarioServiceAPI.save(lu);
        return ResponseEntity.ok(Map.of("mensaje", "Lamina puesta en intercambio."));
    }

    // POST crear lámina (admin)
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Lamina lamina) {
        logger.info("Creando lamina: " + lamina.getNombre());
        Lamina guardada = laminaServiceAPI.save(lamina);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // GET todas las láminas
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(laminaServiceAPI.getAll());
    }

    // GET por selección
    @GetMapping("/seleccion/{seleccion}")
    public ResponseEntity<?> getBySeleccion(@PathVariable String seleccion) {
        List<Lamina> laminas = laminaServiceAPI.findBySeleccion(seleccion);
        if (laminas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay laminas de " + seleccion));
        }
        return ResponseEntity.ok(laminas);
    }
}
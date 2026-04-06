package co.edu.unbosque.controller;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.entity.PreferenciaUsuario;
import co.edu.unbosque.service.api.PreferenciaUsuarioServiceAPI;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/preferencia")
public class PreferenciaUsuarioRestController {

    private static final Logger logger = Logger.getLogger(PreferenciaUsuarioRestController.class.getName());

    @Autowired
    private PreferenciaUsuarioServiceAPI preferenciaUsuarioServiceAPI;

    // HU-03: Guardar preferencias iniciales
    @PostMapping("/{idUsuario}")
    public ResponseEntity<?> guardarPreferencias(
            @PathVariable Long idUsuario,
            @RequestBody PreferenciaUsuario preferencias) {
        try {
            logger.info("Guardando preferencias para usuario: " + idUsuario);
            Optional<PreferenciaUsuario> existente =
                    preferenciaUsuarioServiceAPI.findByIdUsuario(idUsuario);
            if (existente.isPresent()) {
                PreferenciaUsuario pref = existente.get();
                pref.setSeleccionesFavoritas(preferencias.getSeleccionesFavoritas());
                pref.setCiudadesInteres(preferencias.getCiudadesInteres());
                pref.setNotifPush(preferencias.getNotifPush());
                pref.setNotifEmail(preferencias.getNotifEmail());
                preferenciaUsuarioServiceAPI.save(pref);
                return ResponseEntity.ok(Map.of("mensaje", "Preferencias actualizadas."));
            }
            preferencias.setIdUsuario(idUsuario);
            preferenciaUsuarioServiceAPI.save(preferencias);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Preferencias guardadas."));
        } catch (Exception e) {
            logger.severe("Error guardando preferencias: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // HU-04: Obtener preferencias
    @GetMapping("/{idUsuario}")
    public ResponseEntity<?> obtenerPreferencias(@PathVariable Long idUsuario) {
        Optional<PreferenciaUsuario> pref =
                preferenciaUsuarioServiceAPI.findByIdUsuario(idUsuario);
        if (pref.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay preferencias configuradas."));
        }
        return ResponseEntity.ok(pref.get());
    }

    // HU-04: Editar preferencias
    @PutMapping("/{idUsuario}")
    public ResponseEntity<?> editarPreferencias(
            @PathVariable Long idUsuario,
            @RequestBody PreferenciaUsuario preferencias) {
        Optional<PreferenciaUsuario> existente =
                preferenciaUsuarioServiceAPI.findByIdUsuario(idUsuario);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay preferencias para actualizar."));
        }
        PreferenciaUsuario pref = existente.get();
        pref.setSeleccionesFavoritas(preferencias.getSeleccionesFavoritas());
        pref.setCiudadesInteres(preferencias.getCiudadesInteres());
        pref.setNotifPush(preferencias.getNotifPush());
        pref.setNotifEmail(preferencias.getNotifEmail());
        preferenciaUsuarioServiceAPI.save(pref);
        return ResponseEntity.ok(Map.of("mensaje", "Preferencias actualizadas correctamente."));
    }

    // GET todas las preferencias
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(preferenciaUsuarioServiceAPI.getAll());
    }

    // DELETE preferencias
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        PreferenciaUsuario pref = preferenciaUsuarioServiceAPI.get(id);
        if (pref == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Preferencia no encontrada."));
        }
        preferenciaUsuarioServiceAPI.delete(id);
        return ResponseEntity.ok(Map.of("mensaje", "Preferencia eliminada."));
    }
}
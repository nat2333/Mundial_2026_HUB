package co.edu.unbosque.controller;

import java.util.Date;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.LaminaServiceAPI;
import co.edu.unbosque.service.api.NotificacionServiceAPI;
import co.edu.unbosque.service.api.PollaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/admin")
public class AdminRestController {

    private static final Logger logger = Logger.getLogger(AdminRestController.class.getName());

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    @Autowired
    private EntradaServiceAPI entradaServiceAPI;

    @Autowired
    private NotificacionServiceAPI notificacionServiceAPI;

    @Autowired
    private PollaServiceAPI pollaServiceAPI;

    @Autowired
    private LaminaServiceAPI laminaServiceAPI;

    // =============================================
    // GESTIÓN DE USUARIOS — RF-17 Auth
    // =============================================

    // GET todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<?> getTodosUsuarios() {
        logger.info("Admin consultando todos los usuarios");
        return ResponseEntity.ok(usuarioServiceAPI.getAll());
    }

    // GET usuario por ID
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioServiceAPI.get(id);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Usuario no encontrado."));
        }
        return ResponseEntity.ok(usuario);
    }

    // GET usuarios por rol
    @GetMapping("/usuarios/rol/{rol}")
    public ResponseEntity<?> getUsuariosPorRol(@PathVariable String rol) {
        logger.info("Admin consultando usuarios por rol: " + rol);
        List<Usuario> todos = usuarioServiceAPI.getAll();
        List<Usuario> filtrados = todos.stream()
                .filter(u -> rol.equalsIgnoreCase(u.getRol()))
                .toList();
        return ResponseEntity.ok(Map.of(
            "rol", rol,
            "total", filtrados.size(),
            "usuarios", filtrados
        ));
    }

    // GET resumen de usuarios
    @GetMapping("/usuarios/resumen")
    public ResponseEntity<?> getResumenUsuarios() {
        logger.info("Admin consultando resumen de usuarios");
        List<Usuario> todos = usuarioServiceAPI.getAll();
        long activos = todos.stream().filter(u -> u.getEstado() == 1).count();
        long bloqueados = todos.stream().filter(u -> u.getEstado() == 0).count();
        long verificados = todos.stream().filter(u -> u.isCorreoVerificado()).count();

        Map<String, Long> porRol = new HashMap<>();
        porRol.put("AFICIONADO", todos.stream().filter(u -> "AFICIONADO".equals(u.getRol())).count());
        porRol.put("OPERADOR", todos.stream().filter(u -> "OPERADOR".equals(u.getRol())).count());
        porRol.put("SOPORTE", todos.stream().filter(u -> "SOPORTE".equals(u.getRol())).count());
        porRol.put("COMPLIANCE", todos.stream().filter(u -> "COMPLIANCE".equals(u.getRol())).count());
        porRol.put("ADMIN", todos.stream().filter(u -> "ADMIN".equals(u.getRol())).count());

        return ResponseEntity.ok(Map.of(
            "totalUsuarios", todos.size(),
            "activos", activos,
            "bloqueados", bloqueados,
            "verificados", verificados,
            "porRol", porRol
        ));
    }

    // PUT cambiar rol — RF-17 Auth
    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<?> cambiarRol(
            @PathVariable Long id,
            @RequestParam String nuevoRol,
            HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Cambiando rol usuario: " + id);

            Usuario usuario = usuarioServiceAPI.get(id);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Usuario no encontrado."));
            }

            List<String> rolesValidos = List.of(
                "AFICIONADO", "OPERADOR", "SOPORTE", "COMPLIANCE", "ADMIN");
            if (!rolesValidos.contains(nuevoRol.toUpperCase())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje",
                            "Rol no válido. Use: AFICIONADO, OPERADOR, SOPORTE, COMPLIANCE, ADMIN"));
            }

            String rolAnterior = usuario.getRol();
            usuario.setRol(nuevoRol.toUpperCase());
            usuarioServiceAPI.save(usuario);

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(id);
            evento.setTipoEvento("CAMBIO_ROL");
            evento.setModuloOrigen("ADMIN");
            evento.setDetalle("Rol cambiado de " + rolAnterior + " a " + nuevoRol.toUpperCase());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Rol actualizado exitosamente.",
                "rolAnterior", rolAnterior,
                "rolNuevo", nuevoRol.toUpperCase(),
                "usuario", usuario
            ));
        } catch (Exception e) {
            logger.severe("Error cambiando rol: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // PUT bloquear usuario
    @PutMapping("/usuarios/{id}/bloquear")
    public ResponseEntity<?> bloquearUsuario(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            Usuario usuario = usuarioServiceAPI.get(id);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Usuario no encontrado."));
            }
            usuario.setEstado((byte) 0);
            usuarioServiceAPI.save(usuario);

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(id);
            evento.setTipoEvento("BLOQUEO_CUENTA");
            evento.setModuloOrigen("ADMIN");
            evento.setDetalle("Cuenta bloqueada por administrador: " + usuario.getCorreoUsuario());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            return ResponseEntity.ok(Map.of("mensaje", "Usuario bloqueado exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // PUT desbloquear usuario
    @PutMapping("/usuarios/{id}/desbloquear")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            Usuario usuario = usuarioServiceAPI.get(id);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Usuario no encontrado."));
            }
            usuario.setEstado((byte) 1);
            usuario.setIntentos(0);
            usuarioServiceAPI.save(usuario);

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(id);
            evento.setTipoEvento("DESBLOQUEO_CUENTA");
            evento.setModuloOrigen("ADMIN");
            evento.setDetalle("Cuenta desbloqueada por administrador: " + usuario.getCorreoUsuario());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            return ResponseEntity.ok(Map.of("mensaje", "Usuario desbloqueado exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // DELETE usuario
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            Usuario usuario = usuarioServiceAPI.get(id);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Usuario no encontrado."));
            }

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(id);
            evento.setTipoEvento("ELIMINACION_USUARIO");
            evento.setModuloOrigen("ADMIN");
            evento.setDetalle("Usuario eliminado: " + usuario.getCorreoUsuario());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            usuarioServiceAPI.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error: " + e.getMessage()));
        }
    }

    // =============================================
    // PANEL DE MÉTRICAS GENERALES
    // =============================================
    @GetMapping("/metricas")
    public ResponseEntity<?> getMetricas() {
        logger.info("Admin consultando metricas generales");
        List<Usuario> usuarios = usuarioServiceAPI.getAll();

        return ResponseEntity.ok(Map.of(
            "totalUsuarios", usuarios.size(),
            "usuariosActivos", usuarios.stream().filter(u -> u.getEstado() == 1).count(),
            "usuariosBloqueados", usuarios.stream().filter(u -> u.getEstado() == 0).count(),
            "totalEntradas", entradaServiceAPI.getAll().size(),
            "entradasPagadas", entradaServiceAPI.findByEstado("PAGADA").size(),
            "entradasDisponibles", entradaServiceAPI.findByEstado("DISPONIBLE").size(),
            "totalNotificaciones", notificacionServiceAPI.getAll().size(),
            "totalPollas", pollaServiceAPI.getAll().size(),
            "totalLaminas", laminaServiceAPI.getAll().size(),
            "totalEventosAuditoria", eventoAuditoriaServiceAPI.getAll().size()
        ));
    }

    // =============================================
    // AUDITORÍA
    // =============================================
    @GetMapping("/auditoria")
    public ResponseEntity<?> getAuditoria() {
        logger.info("Admin consultando auditoria completa");
        return ResponseEntity.ok(eventoAuditoriaServiceAPI.getAll());
    }

    @GetMapping("/auditoria/usuario/{idUsuario}")
    public ResponseEntity<?> getAuditoriaUsuario(@PathVariable Long idUsuario) {
        logger.info("Admin consultando auditoria usuario: " + idUsuario);
        List<EventoAuditoria> eventos = eventoAuditoriaServiceAPI.findByIdUsuario(idUsuario);
        if (eventos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No hay eventos para este usuario."));
        }
        return ResponseEntity.ok(eventos);
    }
}
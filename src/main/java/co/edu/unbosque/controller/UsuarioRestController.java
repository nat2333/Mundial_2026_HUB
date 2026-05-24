package co.edu.unbosque.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.dto.LoginRequest;
import co.edu.unbosque.dto.RegistroRequest;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.service.impl.EmailService;
import co.edu.unbosque.utils.JwtUtil;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuario")
public class UsuarioRestController {

    private static final Logger logger = Logger.getLogger(UsuarioRestController.class.getName());

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    //Registro de usuario con verificación de correo
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest request,
                                      HttpServletRequest httpRequest) {
        try {
            String correlacion = Utilidad.generarIdCorrelacion();
            logger.info("[" + correlacion + "] Iniciando registro para: " + request.getCorreoUsuario());

            if (usuarioServiceAPI.findByCorreoUsuario(request.getCorreoUsuario()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("mensaje", "El correo ya está registrado."));
            }

            Utilidad util = new Utilidad();
            String token = UUID.randomUUID().toString();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 24);
            Date expiracion = cal.getTime();

            Usuario nuevo = new Usuario();
            nuevo.setCorreoUsuario(request.getCorreoUsuario());
            nuevo.setClaveUsuario(util.generarHash(request.getClaveUsuario()));
            nuevo.setNombres(request.getNombres().toLowerCase());
            nuevo.setApellidos(request.getApellidos().toLowerCase());
            nuevo.setRol("AFICIONADO");
            nuevo.setEstado((byte) 1);
            nuevo.setIntentos(0);
            nuevo.setFechaRegistro(new Date());
            nuevo.setCorreoVerificado(false);
            nuevo.setTokenVerificacion(token);
            nuevo.setFechaExpiracionToken(expiracion);

            Usuario guardado = usuarioServiceAPI.save(nuevo);

            // Enviar correo de verificación
            emailService.enviarCorreoVerificacion(
                guardado.getCorreoUsuario(),
                guardado.getNombres(),
                token
            );

            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(guardado.getId());
            evento.setTipoEvento("REGISTRO");
            evento.setModuloOrigen("AUTH");
            evento.setDetalle("Usuario registrado (pendiente verificación): " + guardado.getCorreoUsuario());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            logger.info("[" + correlacion + "] Registro exitoso, correo de verificación enviado: " + guardado.getCorreoUsuario());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Registro exitoso. Revisa tu correo para verificar tu cuenta."));

        } catch (Exception e) {
            logger.severe("Error en registro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno: " + e.getMessage()));
        }
    }

    // Verificación de correo
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarCorreo(@RequestParam("token") String token,
                                             HttpServletRequest httpRequest) {
        try {
            Optional<Usuario> usuarioOpt = usuarioServiceAPI.findByTokenVerificacion(token);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "Token de verificación inválido."));
            }

            Usuario usuario = usuarioOpt.get();

            if (usuario.isCorreoVerificado()) {
                return ResponseEntity.ok(Map.of("mensaje", "El correo ya fue verificado anteriormente."));
            }

            if (usuario.getFechaExpiracionToken() != null
                    && usuario.getFechaExpiracionToken().before(new Date())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "El enlace de verificación ha expirado. Por favor regístrate nuevamente."));
            }

            usuario.setCorreoVerificado(true);
            usuario.setTokenVerificacion(null);
            usuario.setFechaExpiracionToken(null);
            usuarioServiceAPI.save(usuario);

            // Enviar correo de bienvenida
            emailService.enviarCorreoBienvenida(usuario.getCorreoUsuario(), usuario.getNombres());

            String correlacion = Utilidad.generarIdCorrelacion();
            EventoAuditoria evento = new EventoAuditoria();
            evento.setIdCorrelacion(correlacion);
            evento.setIdUsuario(usuario.getId());
            evento.setTipoEvento("VERIFICACION_CORREO");
            evento.setModuloOrigen("AUTH");
            evento.setDetalle("Correo verificado: " + usuario.getCorreoUsuario());
            evento.setTimestampEvento(new Date());
            evento.setEstadoResultado("OK");
            evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
            eventoAuditoriaServiceAPI.save(evento);

            logger.info("[" + correlacion + "] Correo verificado exitosamente: " + usuario.getCorreoUsuario());
            return ResponseEntity.ok(Map.of("mensaje", "¡Correo verificado exitosamente! Ya puedes iniciar sesión."));

        } catch (Exception e) {
            logger.severe("Error en verificación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno: " + e.getMessage()));
        }
    }

    //Inicio de sesión
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String correlacion = Utilidad.generarIdCorrelacion();
        logger.info("[" + correlacion + "] Intento de login: " + request.getCorreoUsuario());

        Optional<Usuario> userOpt = usuarioServiceAPI.findByCorreoUsuario(request.getCorreoUsuario());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Usuario no encontrado."));
        }

        Usuario user = userOpt.get();

        if (!user.isCorreoVerificado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Debes verificar tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada."));
        }

        if (user.getEstado() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Cuenta bloqueada. Contacta soporte."));
        }

        Utilidad util = new Utilidad();
        if (!user.getClaveUsuario().equals(util.generarHash(request.getClaveUsuario()))) {
            user.setIntentos(user.getIntentos() + 1);
            if (user.getIntentos() >= 3) {
                user.setEstado((byte) 0);
                usuarioServiceAPI.save(user);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "Cuenta bloqueada por múltiples intentos fallidos."));
            }
            usuarioServiceAPI.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Contraseña incorrecta."));
        }

        user.setIntentos(0);
        usuarioServiceAPI.save(user);

        EventoAuditoria evento = new EventoAuditoria();
        evento.setIdCorrelacion(correlacion);
        evento.setIdUsuario(user.getId());
        evento.setTipoEvento("LOGIN");
        evento.setModuloOrigen("AUTH");
        evento.setDetalle("Login exitoso: " + user.getCorreoUsuario());
        evento.setTimestampEvento(new Date());
        evento.setEstadoResultado("OK");
        evento.setIpOrigen(Utilidad.obtenerIp(httpRequest));
        eventoAuditoriaServiceAPI.save(evento);

        String token = jwtUtil.generarToken(user);

        // Payload seguro — nunca exponer la clave hasheada
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("nombres", user.getNombres());
        userData.put("apellidos", user.getApellidos());
        userData.put("correoUsuario", user.getCorreoUsuario());
        userData.put("rol", user.getRol());
        userData.put("tienePreferencias", false);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Login exitoso.");
        response.put("token", token);
        response.put("usuario", userData);
        return ResponseEntity.ok(response);
    }

    // GET todos los usuarios
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(usuarioServiceAPI.getAll());
    }

    // GET usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Usuario usuario = usuarioServiceAPI.get(id);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Usuario no encontrado."));
        }
        return ResponseEntity.ok(usuario);
    }

    // DELETE usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Usuario usuario = usuarioServiceAPI.get(id);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Usuario no encontrado."));
        }
        usuarioServiceAPI.delete(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado."));
    }
}

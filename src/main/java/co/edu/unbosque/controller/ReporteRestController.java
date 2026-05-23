package co.edu.unbosque.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.MiembroPolla;
import co.edu.unbosque.entity.Notificacion;
import co.edu.unbosque.entity.Polla;
import co.edu.unbosque.entity.Pronostico;
import co.edu.unbosque.entity.ReembolsoEntrada;
import co.edu.unbosque.entity.Reporte;
import co.edu.unbosque.entity.TransferenciaEntrada;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.MiembroPollaServiceAPI;
import co.edu.unbosque.service.api.NotificacionServiceAPI;
import co.edu.unbosque.service.api.PollaServiceAPI;
import co.edu.unbosque.service.api.PronosticoServiceAPI;
import co.edu.unbosque.service.api.ReembolsoEntradaServiceAPI;
import co.edu.unbosque.service.api.ReporteServiceAPI;
import co.edu.unbosque.service.api.TransferenciaEntradaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/reporte")
public class ReporteRestController {

    private static final Logger logger = Logger.getLogger(ReporteRestController.class.getName());

    @Autowired
    private ReporteServiceAPI reporteServiceAPI;

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private EntradaServiceAPI entradaServiceAPI;

    @Autowired
    private NotificacionServiceAPI notificacionServiceAPI;

    @Autowired
    private PollaServiceAPI pollaServiceAPI;

    @Autowired
    private MiembroPollaServiceAPI miembroPollaServiceAPI;

    @Autowired
    private PronosticoServiceAPI pronosticoServiceAPI;

    @Autowired
    private TransferenciaEntradaServiceAPI transferenciaEntradaServiceAPI;

    @Autowired
    private ReembolsoEntradaServiceAPI reembolsoEntradaServiceAPI;

    // =============================================
    // REPORTE 1 — Usuarios registrados
    // =============================================
    @GetMapping("/usuarios")
    public ResponseEntity<?> reporteUsuarios() {
        logger.info("Generando reporte de usuarios");
        List<Usuario> todos = usuarioServiceAPI.getAll();

        long activos = todos.stream().filter(u -> u.getEstado() == 1).count();
        long bloqueados = todos.stream().filter(u -> u.getEstado() == 0).count();
        long verificados = todos.stream().filter(u -> u.isCorreoVerificado()).count();
        long noVerificados = todos.stream().filter(u -> !u.isCorreoVerificado()).count();

        Map<String, Long> porRol = new HashMap<>();
        porRol.put("AFICIONADO", todos.stream().filter(u -> "AFICIONADO".equals(u.getRol())).count());
        porRol.put("OPERADOR", todos.stream().filter(u -> "OPERADOR".equals(u.getRol())).count());
        porRol.put("SOPORTE", todos.stream().filter(u -> "SOPORTE".equals(u.getRol())).count());
        porRol.put("COMPLIANCE", todos.stream().filter(u -> "COMPLIANCE".equals(u.getRol())).count());
        porRol.put("ADMIN", todos.stream().filter(u -> "ADMIN".equals(u.getRol())).count());

        // Registrar reporte
        Reporte reporte = new Reporte();
        reporte.setTipoReporte("USUARIOS");
        reporte.setNombre("Reporte de Usuarios Registrados");
        reporte.setFechaGeneracion(new Date());
        reporte.setEstado("GENERADO");
        reporteServiceAPI.save(reporte);

        return ResponseEntity.ok(Map.of(
            "titulo", "Reporte 1 — Usuarios Registrados",
            "fechaGeneracion", new Date(),
            "resumen", Map.of(
                "totalUsuarios", todos.size(),
                "activos", activos,
                "bloqueados", bloqueados,
                "verificados", verificados,
                "noVerificados", noVerificados
            ),
            "porRol", porRol,
            "detalle", todos
        ));
    }

    // =============================================
    // REPORTE 2 — Entradas por partido
    // =============================================
    @GetMapping("/entradas")
    public ResponseEntity<?> reporteEntradas() {
        logger.info("Generando reporte de entradas por partido");
        List<Entrada> todas = entradaServiceAPI.getAll();

        long disponibles = todas.stream().filter(e -> "DISPONIBLE".equals(e.getEstado())).count();
        long reservadas = todas.stream().filter(e -> "RESERVADA".equals(e.getEstado())).count();
        long pagadas = todas.stream().filter(e -> "PAGADA".equals(e.getEstado())).count();
        long transferidas = todas.stream().filter(e -> "TRANSFERIDA".equals(e.getEstado())).count();
        long reembolsadas = todas.stream().filter(e -> "REEMBOLSADA".equals(e.getEstado())).count();
        long expiradas = todas.stream().filter(e -> "EXPIRADA".equals(e.getEstado())).count();

        double totalRecaudado = todas.stream()
                .filter(e -> "PAGADA".equals(e.getEstado()) || "TRANSFERIDA".equals(e.getEstado()))
                .mapToDouble(e -> e.getPrecio() != null ? e.getPrecio() : 0)
                .sum();

        Reporte reporte = new Reporte();
        reporte.setTipoReporte("ENTRADAS");
        reporte.setNombre("Reporte de Entradas por Partido");
        reporte.setFechaGeneracion(new Date());
        reporte.setEstado("GENERADO");
        reporteServiceAPI.save(reporte);

        return ResponseEntity.ok(Map.of(
            "titulo", "Reporte 2 — Entradas por Partido",
            "fechaGeneracion", new Date(),
            "resumen", Map.of(
                "totalEntradas", todas.size(),
                "disponibles", disponibles,
                "reservadas", reservadas,
                "pagadas", pagadas,
                "transferidas", transferidas,
                "reembolsadas", reembolsadas,
                "expiradas", expiradas,
                "totalRecaudado", totalRecaudado
            ),
            "detalle", todas
        ));
    }

    // =============================================
    // REPORTE 3 — Notificaciones enviadas
    // =============================================
    @GetMapping("/notificaciones")
    public ResponseEntity<?> reporteNotificaciones() {
        logger.info("Generando reporte de notificaciones");
        List<Notificacion> todas = notificacionServiceAPI.getAll();

        long porEmail = todas.stream().filter(n -> "EMAIL".equals(n.getCanal())).count();
        long porPush = todas.stream().filter(n -> "PUSH".equals(n.getCanal())).count();
        long enviadas = todas.stream().filter(n -> "ENVIADA".equals(n.getEstadoEnvio())).count();
        long fallidas = todas.stream().filter(n -> "FALLIDA".equals(n.getEstadoEnvio())).count();

        Map<String, Long> porTipo = new HashMap<>();
        porTipo.put("INICIO_PARTIDO", todas.stream().filter(n -> "INICIO_PARTIDO".equals(n.getTipo())).count());
        porTipo.put("GOL", todas.stream().filter(n -> "GOL".equals(n.getTipo())).count());
        porTipo.put("CAMBIO_HORARIO", todas.stream().filter(n -> "CAMBIO_HORARIO".equals(n.getTipo())).count());
        porTipo.put("MASIVA", todas.stream().filter(n -> "MASIVA".equals(n.getTipo())).count());

        Reporte reporte = new Reporte();
        reporte.setTipoReporte("NOTIFICACIONES");
        reporte.setNombre("Reporte de Notificaciones Enviadas");
        reporte.setFechaGeneracion(new Date());
        reporte.setEstado("GENERADO");
        reporteServiceAPI.save(reporte);

        return ResponseEntity.ok(Map.of(
            "titulo", "Reporte 3 — Notificaciones Enviadas",
            "fechaGeneracion", new Date(),
            "resumen", Map.of(
                "totalNotificaciones", todas.size(),
                "porEmail", porEmail,
                "porPush", porPush,
                "enviadas", enviadas,
                "fallidas", fallidas
            ),
            "porTipo", porTipo,
            "detalle", todas
        ));
    }

    // =============================================
    // REPORTE 4 — Pollas y participación
    // =============================================
    @GetMapping("/pollas")
    public ResponseEntity<?> reportePollas() {
        logger.info("Generando reporte de pollas");
        List<Polla> todas = pollaServiceAPI.getAll();
        List<MiembroPolla> miembros = miembroPollaServiceAPI.getAll();
        List<Pronostico> pronosticos = pronosticoServiceAPI.getAll();

        long pollasActivas = todas.stream().filter(p -> "ACTIVA".equals(p.getEstado())).count();
        long pollasCerradas = todas.stream().filter(p -> "CERRADA".equals(p.getEstado())).count();

        List<Map<String, Object>> detallePorPolla = todas.stream().map(p -> {
            Map<String, Object> detalle = new HashMap<>();
            detalle.put("id", p.getId());
            detalle.put("nombre", p.getNombre());
            detalle.put("estado", p.getEstado());
            detalle.put("codigoInvitacion", p.getCodigoInvitacion());
            long totalMiembros = miembros.stream().filter(m -> m.getIdPolla().equals(p.getId())).count();
            long totalPronosticos = pronosticos.stream().filter(pr -> pr.getIdPolla().equals(p.getId())).count();
            detalle.put("totalMiembros", totalMiembros);
            detalle.put("totalPronosticos", totalPronosticos);
            return detalle;
        }).collect(Collectors.toList());

        Reporte reporte = new Reporte();
        reporte.setTipoReporte("POLLAS");
        reporte.setNombre("Reporte de Pollas y Participacion");
        reporte.setFechaGeneracion(new Date());
        reporte.setEstado("GENERADO");
        reporteServiceAPI.save(reporte);

        return ResponseEntity.ok(Map.of(
            "titulo", "Reporte 4 — Pollas y Participacion",
            "fechaGeneracion", new Date(),
            "resumen", Map.of(
                "totalPollas", todas.size(),
                "pollasActivas", pollasActivas,
                "pollasCerradas", pollasCerradas,
                "totalMiembros", miembros.size(),
                "totalPronosticos", pronosticos.size()
            ),
            "detallePorPolla", detallePorPolla
        ));
    }

    // =============================================
    // REPORTE 5 — Transacciones de entradas
    // =============================================
    @GetMapping("/transacciones")
    public ResponseEntity<?> reporteTransacciones() {
        logger.info("Generando reporte de transacciones");
        List<TransferenciaEntrada> transferencias = transferenciaEntradaServiceAPI.getAll();
        List<ReembolsoEntrada> reembolsos = reembolsoEntradaServiceAPI.getAll();
        List<Entrada> entradas = entradaServiceAPI.getAll();

        long pagosConfirmados = entradas.stream()
                .filter(e -> "PAGADA".equals(e.getEstado()) || "TRANSFERIDA".equals(e.getEstado()))
                .count();
        double totalPagado = entradas.stream()
                .filter(e -> "PAGADA".equals(e.getEstado()) || "TRANSFERIDA".equals(e.getEstado()))
                .mapToDouble(e -> e.getPrecio() != null ? e.getPrecio() : 0)
                .sum();

        long reembolsosPendientes = reembolsos.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado())).count();
        long reembolsosAprobados = reembolsos.stream()
                .filter(r -> "APROBADO".equals(r.getEstado())).count();
        long reembolsosRechazados = reembolsos.stream()
                .filter(r -> "RECHAZADO".equals(r.getEstado())).count();

        Reporte reporte = new Reporte();
        reporte.setTipoReporte("TRANSACCIONES");
        reporte.setNombre("Reporte de Transacciones de Entradas");
        reporte.setFechaGeneracion(new Date());
        reporte.setEstado("GENERADO");
        reporteServiceAPI.save(reporte);

        return ResponseEntity.ok(Map.of(
            "titulo", "Reporte 5 — Transacciones de Entradas",
            "fechaGeneracion", new Date(),
            "resumen", Map.of(
                "pagosConfirmados", pagosConfirmados,
                "totalPagado", totalPagado,
                "totalTransferencias", transferencias.size(),
                "totalReembolsos", reembolsos.size(),
                "reembolsosPendientes", reembolsosPendientes,
                "reembolsosAprobados", reembolsosAprobados,
                "reembolsosRechazados", reembolsosRechazados
            ),
            "transferencias", transferencias,
            "reembolsos", reembolsos
        ));
    }

    // GET historial de reportes generados
    @GetMapping("/historial")
    public ResponseEntity<?> getHistorial() {
        return ResponseEntity.ok(reporteServiceAPI.getAll());
    }
}
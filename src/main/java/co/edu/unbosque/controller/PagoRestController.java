package co.edu.unbosque.controller;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.dto.PagoRequest;
import co.edu.unbosque.dto.PagoResponse;
import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.ReembolsoEntrada;
import co.edu.unbosque.service.api.PagoServiceAPI;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/pago")
public class PagoRestController {

    private static final Logger logger = Logger.getLogger(PagoRestController.class.getName());

    @Autowired
    private PagoServiceAPI pagoServiceAPI;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /pago/crear-intencion
    // Crea un PaymentIntent en Stripe. El frontend recibe el clientSecret
    // y lo usa con Stripe.js para mostrar el formulario de pago.
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/crear-intencion")
    public ResponseEntity<?> crearIntencionPago(@RequestBody PagoRequest request) {
        try {
            logger.info("Creando intención de pago para entrada: " + request.getIdEntrada());
            PagoResponse response = pagoServiceAPI.crearIntencionPago(request.getIdEntrada());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error al crear PaymentIntent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al conectar con Stripe: " + e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /pago/confirmar
    // El frontend llama esto DESPUÉS de que Stripe confirme el pago en el cliente.
    // Verifica el estado del PaymentIntent directamente en Stripe antes de actualizar la BD.
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarPago(
            @RequestParam String paymentIntentId,
            @RequestParam Long idEntrada) {
        try {
            logger.info("Confirmando pago: " + paymentIntentId + " para entrada: " + idEntrada);
            Entrada entrada = pagoServiceAPI.confirmarPagoConStripe(paymentIntentId, idEntrada);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Pago confirmado exitosamente. Factura generada.",
                "entrada", entrada,
                "paymentIntentId", paymentIntentId
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error al confirmar pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al confirmar pago: " + e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /pago/estado/{paymentIntentId}
    // Consulta el estado actual de un PaymentIntent en Stripe.
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/estado/{paymentIntentId}")
    public ResponseEntity<?> consultarEstado(@PathVariable String paymentIntentId) {
        try {
            Map<String, Object> estado = pagoServiceAPI.consultarEstadoPago(paymentIntentId);
            return ResponseEntity.ok(estado);
        } catch (Exception e) {
            logger.severe("Error consultando estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al consultar Stripe: " + e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /pago/reembolso/{idEntrada}
    // Solicita un reembolso a Stripe y lo registra en la BD.
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/reembolso/{idEntrada}")
    public ResponseEntity<?> procesarReembolso(
            @PathVariable Long idEntrada,
            @RequestParam Long idUsuario,
            @RequestParam(required = false, defaultValue = "Solicitud de reembolso") String motivo) {
        try {
            logger.info("Procesando reembolso para entrada: " + idEntrada);
            ReembolsoEntrada reembolso = pagoServiceAPI.procesarReembolso(idEntrada, idUsuario, motivo);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Reembolso procesado. Estado: " + reembolso.getEstado(),
                "reembolso", reembolso
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error procesando reembolso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al procesar reembolso: " + e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /pago/webhook
    // Endpoint que Stripe llama automáticamente cuando ocurre un evento.
    // Lee el body como bytes crudos para preservar la firma que Stripe verifica.
    // IMPORTANTE: este endpoint debe estar excluido de CSRF en SecurityConfig.
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<?> webhook(
            HttpServletRequest request,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            if (sigHeader == null) {
                logger.warning("Webhook recibido sin cabecera Stripe-Signature.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Cabecera Stripe-Signature requerida."));
            }

            // Leer el body crudo para que la verificación de firma sea correcta
            String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            pagoServiceAPI.procesarEventoWebhook(payload, sigHeader);
            return ResponseEntity.ok(Map.of("recibido", true));

        } catch (SecurityException e) {
            logger.warning("Firma de webhook inválida: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error procesando webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al procesar webhook."));
        }
    }
}

package co.edu.unbosque.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

import co.edu.unbosque.dto.PagoResponse;
import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.EventoAuditoria;
import co.edu.unbosque.entity.ReembolsoEntrada;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.FacturaEntradaServiceAPI;
import co.edu.unbosque.service.api.PagoServiceAPI;
import co.edu.unbosque.service.api.ReembolsoEntradaServiceAPI;
import co.edu.unbosque.utils.Utilidad;
import jakarta.annotation.PostConstruct;

@Service
public class PagoServiceImpl implements PagoServiceAPI {

    private static final Logger logger = Logger.getLogger(PagoServiceImpl.class.getName());

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private EntradaServiceAPI entradaServiceAPI;

    @Autowired
    private ReembolsoEntradaServiceAPI reembolsoEntradaServiceAPI;

    @Autowired
    private FacturaEntradaServiceAPI facturaEntradaServiceAPI;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe inicializado en modo sandbox.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. Crear PaymentIntent — el frontend usa el clientSecret con Stripe.js
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    public PagoResponse crearIntencionPago(Long idEntrada) throws Exception {
        Entrada entrada = entradaServiceAPI.get(idEntrada);
        if (entrada == null) {
            throw new IllegalArgumentException("Entrada no encontrada: " + idEntrada);
        }
        if (!"RESERVADA".equals(entrada.getEstado())) {
            throw new IllegalStateException(
                "Solo se puede iniciar pago de entradas RESERVADAS. Estado: " + entrada.getEstado());
        }
        if (entrada.getFechaExpiracionReserva() != null
                && entrada.getFechaExpiracionReserva().before(new Date())) {
            entrada.setEstado("EXPIRADA");
            entradaServiceAPI.save(entrada);
            throw new IllegalStateException("La reserva ha expirado.");
        }

        // Stripe maneja COP en centavos (1 COP = 100 centavos)
        long amountCOP = Math.round((entrada.getPrecio() != null ? entrada.getPrecio() : 0.0) * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountCOP)
            .setCurrency("cop")
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .putMetadata("idEntrada", idEntrada.toString())
            .putMetadata("idCorrelacion", entrada.getIdCorrelacion() != null
                ? entrada.getIdCorrelacion() : "")
            .build();

        PaymentIntent intent = PaymentIntent.create(params);

        logger.info("PaymentIntent creado: " + intent.getId()
            + " para entrada: " + idEntrada
            + " | monto: " + amountCOP + " COP");
        registrarAuditoria("PAGO_INICIADO", entrada.getIdTitular(), idEntrada,
            "PaymentIntent " + intent.getId() + " | monto: " + amountCOP + " COP", "OK");

        PagoResponse response = new PagoResponse();
        response.setPaymentIntentId(intent.getId());
        response.setClientSecret(intent.getClientSecret());
        response.setAmount(intent.getAmount());
        response.setCurrency(intent.getCurrency());
        response.setStatus(intent.getStatus());
        response.setIdEntrada(idEntrada);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. Confirmar pago verificando el estado del PaymentIntent en Stripe
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    @Override
    public Entrada confirmarPagoConStripe(String paymentIntentId, Long idEntrada) throws Exception {
        Entrada entrada = entradaServiceAPI.get(idEntrada);
        if (entrada == null) {
            throw new IllegalArgumentException("Entrada no encontrada: " + idEntrada);
        }
        if (!"RESERVADA".equals(entrada.getEstado())) {
            throw new IllegalStateException(
                "La entrada no está en estado RESERVADA. Estado: " + entrada.getEstado());
        }

        // Verificar con Stripe solo si la entrada está en estado válido
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        if (!"succeeded".equals(intent.getStatus())) {
            throw new IllegalStateException(
                "El pago en Stripe no está confirmado. Estado: " + intent.getStatus());
        }

        entrada.setEstado("PAGADA");
        entrada.setFechaPago(new Date());
        entrada.setIdTransaccionPago(paymentIntentId);
        entradaServiceAPI.save(entrada);

        try {
            facturaEntradaServiceAPI.generarFactura(idEntrada);
            logger.info("Factura generada para entrada: " + idEntrada);
        } catch (Exception ef) {
            logger.warning("Pago confirmado pero error al generar factura: " + ef.getMessage());
        }

        registrarAuditoria("PAGO_CONFIRMADO", entrada.getIdTitular(), idEntrada,
            "PaymentIntent " + paymentIntentId + " | entrada marcada PAGADA", "OK");
        logger.info("Pago confirmado via Stripe para entrada: " + idEntrada
            + " | PaymentIntent: " + paymentIntentId);
        return entrada;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. Procesar reembolso en Stripe y registrar en la BD
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    @Override
    public ReembolsoEntrada procesarReembolso(Long idEntrada, Long idUsuario, String motivo)
            throws Exception {
        Entrada entrada = entradaServiceAPI.get(idEntrada);
        if (entrada == null) {
            throw new IllegalArgumentException("Entrada no encontrada: " + idEntrada);
        }
        if (!"PAGADA".equals(entrada.getEstado())) {
            throw new IllegalStateException(
                "Solo se pueden reembolsar entradas PAGADAS. Estado: " + entrada.getEstado());
        }
        if (entrada.getIdTransaccionPago() == null || entrada.getIdTransaccionPago().isBlank()) {
            throw new IllegalStateException(
                "La entrada no tiene un PaymentIntent de Stripe asociado.");
        }

        // Crear el Refund en Stripe usando el PaymentIntent ID
        RefundCreateParams refundParams = RefundCreateParams.builder()
            .setPaymentIntent(entrada.getIdTransaccionPago())
            .putMetadata("idEntrada", idEntrada.toString())
            .putMetadata("motivo", motivo != null ? motivo : "")
            .build();

        Refund refund = Refund.create(refundParams);

        logger.info("Reembolso creado en Stripe: " + refund.getId()
            + " | estado: " + refund.getStatus()
            + " | entrada: " + idEntrada);

        // Registrar el reembolso en la BD
        ReembolsoEntrada reembolso = new ReembolsoEntrada();
        reembolso.setIdEntrada(idEntrada);
        reembolso.setIdUsuario(idUsuario);
        reembolso.setMotivo(motivo);
        reembolso.setFechaSolicitud(new Date());
        reembolso.setIdCorrelacion(Utilidad.generarIdCorrelacion());
        reembolso.setIdTransaccionReembolso(refund.getId());

        // Estado inicial según respuesta de Stripe
        if ("succeeded".equals(refund.getStatus())) {
            reembolso.setEstado("APROBADO");
            reembolso.setFechaResolucion(new Date());
            entrada.setEstado("REEMBOLSADA");
            entradaServiceAPI.save(entrada);
        } else if ("pending".equals(refund.getStatus())) {
            reembolso.setEstado("PENDIENTE");
        } else {
            reembolso.setEstado("RECHAZADO");
        }

        ReembolsoEntrada guardado = reembolsoEntradaServiceAPI.save(reembolso);
        registrarAuditoria("REEMBOLSO_SOLICITADO", idUsuario, idEntrada,
            "Refund Stripe " + refund.getId() + " | estado: " + refund.getStatus()
            + " | motivo: " + motivo, guardado.getEstado());
        return guardado;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. Consultar estado de un PaymentIntent en Stripe
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> consultarEstadoPago(String paymentIntentId) throws Exception {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("paymentIntentId", intent.getId());
        resultado.put("status", intent.getStatus());
        resultado.put("amount", intent.getAmount());
        resultado.put("currency", intent.getCurrency());
        resultado.put("created", intent.getCreated());
        resultado.put("metadata", intent.getMetadata());
        return resultado;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. Webhook: recibe eventos de Stripe y los procesa de forma segura
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    public void procesarEventoWebhook(String payload, String sigHeader) throws Exception {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new SecurityException("Firma del webhook de Stripe inválida: " + e.getMessage());
        }

        logger.info("Webhook Stripe recibido: " + event.getType() + " | id: " + event.getId());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> manejarPagoExitoso(event);
            case "payment_intent.payment_failed" -> manejarPagoFallido(event);
            case "charge.refund.updated" -> manejarReembolsoActualizado(event);
            default -> logger.info("Evento no manejado: " + event.getType());
        }
    }

    private void manejarPagoExitoso(Event event) {
        try {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("No se pudo deserializar PaymentIntent"));

            String idEntradaStr = intent.getMetadata().get("idEntrada");
            if (idEntradaStr == null || idEntradaStr.isBlank()) {
                logger.warning("Webhook payment_intent.succeeded sin metadata idEntrada: " + intent.getId());
                return;
            }

            Long idEntrada = Long.parseLong(idEntradaStr);
            Entrada entrada = entradaServiceAPI.get(idEntrada);
            if (entrada == null) {
                logger.warning("Webhook: entrada no encontrada: " + idEntrada);
                return;
            }

            // Idempotencia: si ya está PAGADA no volver a procesar
            if ("PAGADA".equals(entrada.getEstado())) {
                logger.info("Webhook: entrada " + idEntrada + " ya estaba PAGADA, ignorando.");
                return;
            }

            entrada.setEstado("PAGADA");
            entrada.setFechaPago(new Date());
            entrada.setIdTransaccionPago(intent.getId());
            entradaServiceAPI.save(entrada);

            try {
                facturaEntradaServiceAPI.generarFactura(idEntrada);
                logger.info("Webhook: factura generada para entrada " + idEntrada);
            } catch (Exception ef) {
                logger.warning("Webhook: pago OK pero error al generar factura: " + ef.getMessage());
            }

            logger.info("Webhook: entrada " + idEntrada + " marcada PAGADA via " + intent.getId());

        } catch (Exception e) {
            logger.severe("Error procesando payment_intent.succeeded: " + e.getMessage());
        }
    }

    private void manejarPagoFallido(Event event) {
        try {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

            String idEntradaStr = intent.getMetadata().get("idEntrada");
            logger.warning("Pago fallido para PaymentIntent: " + intent.getId()
                + " | entrada: " + idEntradaStr
                + " | motivo: " + intent.getLastPaymentError());

        } catch (Exception e) {
            logger.severe("Error procesando payment_intent.payment_failed: " + e.getMessage());
        }
    }

    private void registrarAuditoria(String tipoEvento, Long idUsuario, Long idEntidad, String detalle, String resultado) {
        try {
            EventoAuditoria ev = new EventoAuditoria();
            ev.setIdCorrelacion(Utilidad.generarIdCorrelacion());
            ev.setIdUsuario(idUsuario);
            ev.setTipoEvento(tipoEvento);
            ev.setModuloOrigen("PAGOS");
            ev.setDetalle(detalle);
            ev.setEntidadAfectada("entrada");
            ev.setIdEntidad(idEntidad);
            ev.setTimestampEvento(new Date());
            ev.setEstadoResultado(resultado);
            eventoAuditoriaServiceAPI.save(ev);
        } catch (Exception e) {
            logger.warning("No se pudo registrar auditoría [" + tipoEvento + "]: " + e.getMessage());
        }
    }

    private void manejarReembolsoActualizado(Event event) {
        try {
            Refund refund = (Refund) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

            logger.info("Reembolso actualizado: " + refund.getId() + " | estado: " + refund.getStatus());

            reembolsoEntradaServiceAPI.findByIdTransaccionReembolso(refund.getId())
                .ifPresent(reembolso -> {
                    if ("succeeded".equals(refund.getStatus())) {
                        reembolso.setEstado("APROBADO");
                        reembolso.setFechaResolucion(new Date());
                    } else if ("failed".equals(refund.getStatus())) {
                        reembolso.setEstado("RECHAZADO");
                        reembolso.setFechaResolucion(new Date());
                    }
                    reembolsoEntradaServiceAPI.save(reembolso);
                    logger.info("Reembolso " + refund.getId() + " actualizado a: " + reembolso.getEstado());
                });

        } catch (Exception e) {
            logger.severe("Error procesando charge.refund.updated: " + e.getMessage());
        }
    }
}

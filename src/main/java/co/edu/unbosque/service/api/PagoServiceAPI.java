package co.edu.unbosque.service.api;

import java.util.Map;

import co.edu.unbosque.dto.PagoResponse;
import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.ReembolsoEntrada;

public interface PagoServiceAPI {

    /** Crea un PaymentIntent en Stripe y devuelve el clientSecret al frontend. */
    PagoResponse crearIntencionPago(Long idEntrada) throws Exception;

    /** Verifica con Stripe que el PaymentIntent está 'succeeded' y marca la entrada como PAGADA. */
    Entrada confirmarPagoConStripe(String paymentIntentId, Long idEntrada) throws Exception;

    /** Crea un Refund en Stripe y registra el reembolso en la BD. */
    ReembolsoEntrada procesarReembolso(Long idEntrada, Long idUsuario, String motivo) throws Exception;

    /** Consulta el estado actual de un PaymentIntent directamente en Stripe. */
    Map<String, Object> consultarEstadoPago(String paymentIntentId) throws Exception;

    /** Procesa los eventos enviados por el webhook de Stripe (verificando la firma). */
    void procesarEventoWebhook(String payload, String sigHeader) throws Exception;
}

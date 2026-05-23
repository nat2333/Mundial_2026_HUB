package co.edu.unbosque.service.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarCorreoVerificacion(String destinatario, String nombres, String token) {
        String enlace = frontendUrl + "/verificar?token=" + token;
        String asunto = "Mundial 2026 Hub - Verifica tu correo";
        enviar(destinatario, asunto, templateVerificacion(nombres, enlace));
    }

    public void enviarCorreoBienvenida(String destinatario, String nombres) {
        String asunto = "¡Bienvenido a Mundial 2026 Hub!";
        enviar(destinatario, asunto, templateBienvenida(nombres));
    }

    public void enviarFacturaConAdjunto(String destinatario, String nombreCompleto,
                                        String numeroFactura, byte[] pdfBytes) {
        String asunto = "Mundial 2026 Hub - Comprobante " + numeroFactura;
        enviarConAdjunto(destinatario, asunto,
                templateFactura(nombreCompleto, numeroFactura),
                numeroFactura + ".pdf", pdfBytes);
    }

    public void enviarNotificacion(String destinatario, String nombres,
                                   String tipo, String mensaje) {
        enviar(destinatario, "Mundial 2026 Hub - " + tipo,
                templateNotificacion(nombres, tipo, mensaje));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Envío base
    // ─────────────────────────────────────────────────────────────────────────

    private void enviar(String destinatario, String asunto, String html) {
        try {
            Mail mail = construirMail(destinatario, asunto, html);
            ejecutar(mail, destinatario);
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar correo a " + destinatario + ": " + e.getMessage(), e);
        }
    }

    private void enviarConAdjunto(String destinatario, String asunto, String html,
                                   String nombreArchivo, byte[] bytes) {
        try {
            Mail mail = construirMail(destinatario, asunto, html);

            Attachments adjunto = new Attachments();
            adjunto.setContent(Base64.getEncoder().encodeToString(bytes));
            adjunto.setFilename(nombreArchivo);
            adjunto.setType("application/pdf");
            adjunto.setDisposition("attachment");
            mail.addAttachments(adjunto);

            ejecutar(mail, destinatario);
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar correo con adjunto a " + destinatario + ": " + e.getMessage(), e);
        }
    }

    private Mail construirMail(String destinatario, String asunto, String html) {
        Email from    = new Email(fromEmail, fromName);
        Email to      = new Email(destinatario);
        Content body  = new Content("text/html", html);
        return new Mail(from, asunto, to, body);
    }

    private void ejecutar(Mail mail, String destinatario) throws IOException {
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 400) {
            logger.warning("SendGrid error " + response.getStatusCode()
                    + " al enviar a " + destinatario + ": " + response.getBody());
            throw new RuntimeException(
                    "Error SendGrid [" + response.getStatusCode() + "]: " + response.getBody());
        }
        logger.info("Correo enviado a " + destinatario + " — HTTP " + response.getStatusCode());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Templates HTML — diseño cobalt-blue (#304ffe) que hace match al frontend
    // ─────────────────────────────────────────────────────────────────────────

    private String templateVerificacion(String nombres, String enlace) {
        return base(
            "Verifica tu cuenta",
            "<h2 style='margin:0 0 16px;color:#1a1a2e;font-size:22px;'>Hola, " + nombres + "!</h2>" +
            "<p style='color:#444;font-size:15px;line-height:1.7;margin:0 0 12px;'>Gracias por registrarte en <strong>Mundial 2026 Hub</strong>." +
            " Para activar tu cuenta haz clic en el botón de abajo.</p>" +
            "<p style='color:#888;font-size:13px;margin:0 0 28px;'>Este enlace expira en <strong>24 horas</strong>.</p>" +
            boton("Verificar mi correo", enlace) +
            "<p style='color:#aaa;font-size:12px;margin:28px 0 0;'>Si el botón no funciona, copia este enlace en tu navegador:</p>" +
            "<p style='word-break:break-all;color:#304ffe;font-size:12px;margin:6px 0 0;'>" + enlace + "</p>" +
            "<p style='color:#bbb;font-size:12px;margin:24px 0 0;border-top:1px solid #eee;padding-top:18px;'>" +
            "Si no creaste esta cuenta, puedes ignorar este mensaje.</p>"
        );
    }

    private String templateBienvenida(String nombres) {
        return base(
            "¡Bienvenido!",
            "<h2 style='margin:0 0 8px;color:#304ffe;font-size:24px;'>¡Bienvenido, " + nombres + "!</h2>" +
            "<p style='color:#444;font-size:15px;line-height:1.7;margin:0 0 22px;'>" +
            "Tu cuenta ha sido verificada. Ya puedes disfrutar todo lo que <strong>Mundial 2026 Hub</strong> tiene para ti.</p>" +
            "<div style='background:#f4f6ff;border-radius:10px;padding:20px 24px;margin:0 0 24px;'>" +
            "<p style='margin:0 0 12px;color:#1a1a2e;font-weight:bold;font-size:14px;'>¿Qué puedes hacer?</p>" +
            "<ul style='margin:0;padding-left:18px;color:#555;font-size:14px;line-height:2.2;'>" +
            "<li>Seguir tus equipos favoritos</li>" +
            "<li>Ver el calendario de partidos</li>" +
            "<li>Explorar las sedes del Mundial 2026</li>" +
            "<li>Participar en pollas con amigos</li>" +
            "<li>Comprar y gestionar tus entradas</li>" +
            "</ul></div>" +
            boton("Ir a la plataforma", frontendUrl)
        );
    }

    private String templateFactura(String nombreCompleto, String numeroFactura) {
        return base(
            "Comprobante de compra",
            "<h2 style='margin:0 0 16px;color:#1a1a2e;font-size:22px;'>Hola, " + nombreCompleto + "!</h2>" +
            "<p style='color:#444;font-size:15px;line-height:1.7;margin:0 0 20px;'>" +
            "Tu compra ha sido procesada exitosamente. Adjunto encontrarás tu <strong>comprobante en PDF</strong>.</p>" +
            "<div style='background:#f4f6ff;border-left:4px solid #304ffe;border-radius:0 8px 8px 0;padding:14px 18px;margin:0 0 22px;'>" +
            "<p style='margin:0;color:#1a1a2e;font-size:14px;'><strong>N.° Factura:</strong> " + numeroFactura + "</p>" +
            "</div>" +
            "<p style='color:#555;font-size:14px;line-height:1.7;margin:0 0 24px;'>" +
            "Guarda este comprobante: lo necesitarás para acceder al estadio el día del partido. " +
            "También puedes descargarlo desde tu perfil en la plataforma.</p>" +
            "<p style='color:#aaa;font-size:12px;margin:0;border-top:1px solid #eee;padding-top:18px;'>" +
            "Si no realizaste esta compra, contacta a soporte de inmediato.</p>"
        );
    }

    private String templateNotificacion(String nombres, String tipo, String mensaje) {
        return base(
            tipo,
            "<h2 style='margin:0 0 16px;color:#1a1a2e;font-size:22px;'>Hola, " + nombres + "!</h2>" +
            "<p style='color:#444;font-size:15px;line-height:1.7;margin:0;'>" + mensaje + "</p>"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layout base compartido
    // ─────────────────────────────────────────────────────────────────────────

    private String base(String titulo, String contenido) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
            "<body style='margin:0;padding:0;background-color:#eef0ff;font-family:Inter,Arial,Helvetica,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' role='presentation'>" +
            "<tr><td align='center' style='padding:40px 16px;'>" +
            "<table width='560' cellpadding='0' cellspacing='0' role='presentation' " +
            "style='background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(48,79,254,.12);'>" +

            // Header cobalt-blue
            "<tr><td style='background:linear-gradient(135deg,#304ffe 0%,#4361ee 100%);padding:28px 32px;text-align:center;'>" +
            "<p style='margin:0;color:rgba(255,255,255,.7);font-size:11px;letter-spacing:3px;text-transform:uppercase;font-weight:600;'>FIFA WORLD CUP 2026</p>" +
            "<h1 style='margin:6px 0 0;color:#ffffff;font-size:22px;font-weight:800;letter-spacing:1px;'>MUNDIAL 2026 HUB</h1>" +
            "<p style='margin:6px 0 0;color:rgba(255,255,255,.8);font-size:13px;'>" + titulo + "</p>" +
            "</td></tr>" +

            // Contenido
            "<tr><td style='padding:36px 32px;'>" + contenido + "</td></tr>" +

            // Footer
            "<tr><td style='background:#0a0e1a;padding:18px 32px;text-align:center;'>" +
            "<p style='margin:0;color:#5a6080;font-size:11px;'>© 2026 Mundial Hub · Universidad El Bosque</p>" +
            "</td></tr>" +
            "</table></td></tr></table></body></html>";
    }

    private String boton(String texto, String href) {
        return "<div style='text-align:center;margin:28px 0;'>" +
            "<a href='" + href + "' style='display:inline-block;background:#304ffe;color:#ffffff;" +
            "padding:14px 36px;border-radius:8px;text-decoration:none;font-size:15px;font-weight:700;" +
            "letter-spacing:.3px;'>" + texto + "</a></div>";
    }
}

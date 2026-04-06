package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void enviarCorreoVerificacion(String destinatario, String nombres, String token) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("⚽ Mundial 2026 Hub - Verifica tu correo");

            String enlace = frontendUrl + "/verificar?token=" + token;
            String cuerpo = construirCuerpoVerificacion(nombres, enlace);

            helper.setText(cuerpo, true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo de verificación: " + e.getMessage(), e);
        }
    }

    public void enviarCorreoBienvenida(String destinatario, String nombres) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("⚽ ¡Bienvenido a Mundial 2026 Hub!");

            String cuerpo = construirCuerpoBienvenida(nombres);
            helper.setText(cuerpo, true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo de bienvenida: " + e.getMessage(), e);
        }
    }

    private String construirCuerpoVerificacion(String nombres, String enlace) {
        return "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0'>" +
            "<tr><td align='center' style='padding:40px 0;'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background-color:#8B0000;padding:30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;'>⚽ Mundial 2026 Hub</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;'>" +
            "<h2 style='color:#333;margin-top:0;'>Hola, " + nombres + "!</h2>" +
            "<p style='color:#555;font-size:16px;line-height:1.6;'>Gracias por registrarte en <strong>Mundial 2026 Hub</strong>. " +
            "Para activar tu cuenta, por favor verifica tu correo electrónico haciendo clic en el botón de abajo.</p>" +
            "<p style='color:#888;font-size:14px;'>Este enlace expira en <strong>24 horas</strong>.</p>" +
            "<div style='text-align:center;margin:35px 0;'>" +
            "<a href='" + enlace + "' style='background-color:#8B0000;color:#ffffff;padding:14px 32px;text-decoration:none;border-radius:6px;font-size:16px;font-weight:bold;display:inline-block;'>Verificar mi correo</a>" +
            "</div>" +
            "<p style='color:#888;font-size:13px;'>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>" +
            "<p style='word-break:break-all;color:#8B0000;font-size:13px;'>" + enlace + "</p>" +
            "<hr style='border:none;border-top:1px solid #eee;margin:30px 0;'>" +
            "<p style='color:#aaa;font-size:12px;text-align:center;'>Si no creaste esta cuenta, puedes ignorar este correo.</p>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f9f9f9;padding:20px;text-align:center;'>" +
            "<p style='color:#aaa;font-size:12px;margin:0;'>© 2026 Mundial Hub - Universidad El Bosque</p>" +
            "</td></tr>" +
            "</table></td></tr></table>" +
            "</body></html>";
    }

    private String construirCuerpoBienvenida(String nombres) {
        return "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0'>" +
            "<tr><td align='center' style='padding:40px 0;'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background-color:#8B0000;padding:30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;'>⚽ Mundial 2026 Hub</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;text-align:center;'>" +
            "<h2 style='color:#8B0000;font-size:28px;'>¡Bienvenido, " + nombres + "!</h2>" +
            "<p style='color:#555;font-size:16px;line-height:1.6;'>Tu cuenta ha sido verificada exitosamente. " +
            "Ya puedes disfrutar de todo lo que <strong>Mundial 2026 Hub</strong> tiene para ti.</p>" +
            "<div style='background:#f9f9f9;border-radius:8px;padding:25px;margin:25px 0;text-align:left;'>" +
            "<h3 style='color:#333;margin-top:0;'>¿Qué puedes hacer?</h3>" +
            "<ul style='color:#555;font-size:15px;line-height:2;'>" +
            "<li>🏆 Seguir tus equipos favoritos</li>" +
            "<li>📅 Ver el calendario de partidos</li>" +
            "<li>🌍 Explorar las sedes del Mundial 2026</li>" +
            "<li>🔔 Recibir notificaciones de tus partidos favoritos</li>" +
            "</ul></div>" +
            "<a href='http://localhost:3000/login' style='background-color:#8B0000;color:#ffffff;padding:14px 32px;text-decoration:none;border-radius:6px;font-size:16px;font-weight:bold;display:inline-block;'>Ir al inicio de sesión</a>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f9f9f9;padding:20px;text-align:center;'>" +
            "<p style='color:#aaa;font-size:12px;margin:0;'>© 2026 Mundial Hub - Universidad El Bosque</p>" +
            "</td></tr>" +
            "</table></td></tr></table>" +
            "</body></html>";
    }
}

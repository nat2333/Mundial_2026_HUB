package co.edu.unbosque.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

public class Utilidad {

    /**
     * Obtiene la IP real del cliente respetando proxies y load balancers
     * (CloudFront, AWS ALB, GCP Load Balancer, nginx, Cloudflare, etc.).
     * En despliegue local devuelve la IP del cliente directo.
     *
     * Orden de prioridad:
     *  1. X-Forwarded-For        (estándar de facto para proxies HTTP)
     *  2. X-Real-IP              (nginx, algunos PaaS)
     *  3. CF-Connecting-IP       (Cloudflare)
     *  4. True-Client-IP         (Akamai, Cloudflare Enterprise)
     *  5. request.getRemoteAddr() (fallback local / sin proxy)
     */
    public static String obtenerIp(HttpServletRequest request) {
        if (request == null) return null;
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "True-Client-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };
        for (String h : headers) {
            String v = request.getHeader(h);
            if (v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v)) {
                // X-Forwarded-For puede traer "client, proxy1, proxy2" — la primera es el cliente
                int comma = v.indexOf(',');
                return (comma > 0 ? v.substring(0, comma) : v).trim();
            }
        }
        return request.getRemoteAddr();
    }


    public String generarHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
					hexString.append('0');
				}
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Algoritmo de hash no disponible.", e);
        }
    }

    public static String generarIdCorrelacion() {
        return UUID.randomUUID().toString();
    }
}
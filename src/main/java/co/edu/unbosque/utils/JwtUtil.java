package co.edu.unbosque.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.unbosque.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private SecretKey secretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 requiere mínimo 32 bytes — el secret en properties debe tener >= 32 chars
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getCorreoUsuario())
                .claim("idUsuario", usuario.getId())
                .claim("rol", usuario.getRol())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey())
                .compact();
    }

    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCorreo(String token) {
        return parsearClaims(token).getSubject();
    }

    public String getRol(String token) {
        return parsearClaims(token).get("rol", String.class);
    }

    public Long getIdUsuario(String token) {
        return parsearClaims(token).get("idUsuario", Long.class);
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

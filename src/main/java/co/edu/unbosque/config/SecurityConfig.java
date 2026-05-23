package co.edu.unbosque.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Preflight CORS ───────────────────────────────────────────
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ── AUTH pública ─────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/usuario/registro", "/usuario/login").permitAll()
                .requestMatchers(HttpMethod.GET,  "/usuario/verificar").permitAll()

                // ── Lectura pública: partidos, sedes, láminas ────────────────
                .requestMatchers(HttpMethod.GET,  "/partido/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/sedes/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/lamina/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/paquete-laminas/**").permitAll()

                // ── Lectura pública: pollas y pronósticos ────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/polla/getAll", "/polla/ranking/*", "/polla/miembros/*").permitAll()
                .requestMatchers(HttpMethod.GET,  "/pronostico/polla/*").permitAll()

                // ── Lectura pública: entradas disponibles ────────────────────
                .requestMatchers(HttpMethod.GET,  "/entrada/estado/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/entrada/partido/**").permitAll()

                // ── Stripe webhook: sin JWT, la firma se valida internamente ─
                .requestMatchers(HttpMethod.POST, "/pago/webhook").permitAll()

                // ── SOLO ADMIN ───────────────────────────────────────────────
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/backoffice/**").hasRole("ADMIN")
                .requestMatchers("/reporte/**").hasRole("ADMIN")
                .requestMatchers("/auditoria/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/usuario/getAll").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/agenda/getAll").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/factura/getAll").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/partido/save").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/partido/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/partido/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/partido/sincronizar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/partido/debug-api").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/usuario/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/sedes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/sedes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/sedes/**").hasRole("ADMIN")

                // ── Reembolsos: aprobar/rechazar/listar pendientes solo ADMIN ──
                .requestMatchers(HttpMethod.GET,  "/reembolso/pendientes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/reembolso/getAll").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/reembolso/aprobar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/reembolso/rechazar/**").hasRole("ADMIN")

                // ── TODO LO DEMÁS requiere JWT válido ────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}

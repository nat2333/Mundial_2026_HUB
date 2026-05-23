package co.edu.unbosque.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.repository.SedeRepository;
import co.edu.unbosque.service.api.PartidoServiceAPI;

/**
 * HU-25: Adaptador para football-data.org v4.
 * Endpoint: GET /competitions/WC/matches  (código WC = FIFA World Cup)
 * Auth: X-Auth-Token header
 */
@Component
public class ApiDeportesAdapter {

    private static final Logger logger =
        Logger.getLogger(ApiDeportesAdapter.class.getName());

    @Autowired
    private PartidoServiceAPI partidoServiceAPI;

    @Autowired
    private SedeRepository sedeRepository;

    @Value("${football.api.key}")
    private String apiKey;

    @Value("${football.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Partido> sincronizarPartidos() {
        try {
            logger.info("Consultando football-data.org — WC 2026...");

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = apiUrl + "/competitions/WC/matches";
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            logger.info("Respuesta recibida de football-data.org.");
            return parsearPartidos(response.getBody());

        } catch (Exception e) {
            logger.warning("API externa no disponible: " + e.getMessage());
            return marcarComoPendientes(partidoServiceAPI.getAll());
        }
    }

    private List<Partido> parsearPartidos(String json) {
        List<Partido> partidos = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode matches = root.path("matches");

            logger.info("Partidos recibidos de la API: " + matches.size());

            // fecha formato: "2026-06-11T23:00:00Z"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            for (JsonNode match : matches) {
                Partido partido = new Partido();

                partido.setIdExterno(match.path("id").asText());
                partido.setEquipoLocal(
                    match.path("homeTeam").path("name").asText("Por definir"));
                partido.setEquipoVisitante(
                    match.path("awayTeam").path("name").asText("Por definir"));

                // venue puede ser null en algunos partidos — buscar la Sede por nombre
                String venue = match.path("venue").asText("");
                if (!venue.isBlank()) {
                    sedeRepository.findByNombreEstadioIgnoreCase(venue)
                        .ifPresent(partido::setSede);
                }

                // Usar grupo específico si existe (GROUP_A, GROUP_B…), si no la fase general
                String group = match.path("group").asText("");
                String stage = match.path("stage").asText("GROUP_STAGE");
                partido.setFase(group.isBlank() ? stage : group);
                partido.setEstado(mapearEstado(match.path("status").asText()));

                // Goles (fullTime puede ser null antes del partido)
                JsonNode fullTime = match.path("score").path("fullTime");
                partido.setGolesLocal(
                    fullTime.path("home").isNull() ? null : fullTime.path("home").asInt());
                partido.setGolesVisitante(
                    fullTime.path("away").isNull() ? null : fullTime.path("away").asInt());

                // Fecha
                String utcDate = match.path("utcDate").asText(null);
                if (utcDate != null && !utcDate.isBlank()) {
                    try {
                        partido.setFechaHora(sdf.parse(utcDate));
                    } catch (Exception ex) {
                        logger.warning("No se pudo parsear fecha: " + utcDate);
                    }
                }

                partido.setDatosConfirmados((byte) 1);

                partidoServiceAPI.findByIdExterno(partido.getIdExterno()).ifPresent(existente -> {
                    partido.setId(existente.getId());
                    // conservar sede si la API no la devuelve (partidos futuros sin venue)
                    if (partido.getSede() == null && existente.getSede() != null) {
                        partido.setSede(existente.getSede());
                    }
                });
                partidoServiceAPI.save(partido);
                partidos.add(partido);
            }
            logger.info("Partidos sincronizados: " + partidos.size());
        } catch (Exception e) {
            logger.warning("Error parseando respuesta: " + e.getMessage());
        }
        return partidos;
    }

    private String mapearEstado(String statusApi) {
        switch (statusApi) {
            case "SCHEDULED": case "TIMED": return "PROGRAMADO";
            case "IN_PLAY":   case "PAUSED": return "EN_JUEGO";
            case "FINISHED":               return "FINALIZADO";
            case "POSTPONED": case "CANCELLED":
            case "SUSPENDED":              return "POSPUESTO";
            default:                       return "PROGRAMADO";
        }
    }

    private List<Partido> marcarComoPendientes(List<Partido> partidos) {
        partidos.forEach(p -> p.setDatosConfirmados((byte) 0));
        return partidos;
    }

    public String getEstadoConfirmacion(Partido partido) {
        return partido.getDatosConfirmados() == 1
            ? "confirmado" : "actualización pendiente";
    }

    /** Devuelve la respuesta cruda de la API para diagnóstico. */
    public String getRawApiResponse() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = apiUrl + "/competitions/WC/matches";
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}

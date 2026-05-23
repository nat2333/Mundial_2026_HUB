package co.edu.unbosque.controller;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.dto.FacturaDTO;
import co.edu.unbosque.entity.FacturaEntrada;
import co.edu.unbosque.service.api.FacturaEntradaServiceAPI;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/factura")
public class FacturaEntradaRestController {

    private static final Logger logger = Logger.getLogger(FacturaEntradaRestController.class.getName());

    @Autowired
    private FacturaEntradaServiceAPI facturaEntradaServiceAPI;

    // ─────────────────────────────────────────────────────────
    // POST /factura/generar/{idEntrada}
    // Genera (o retorna) la factura de una entrada PAGADA
    // ─────────────────────────────────────────────────────────
    @PostMapping("/generar/{idEntrada}")
    public ResponseEntity<?> generarFactura(@PathVariable Long idEntrada) {
        try {
            logger.info("Solicitud de generacion de factura para entrada: " + idEntrada);
            FacturaEntrada factura = facturaEntradaServiceAPI.generarFactura(idEntrada);
            FacturaDTO dto = facturaEntradaServiceAPI.construirDTO(factura);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error generando factura: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno al generar la factura."));
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET /factura/entrada/{idEntrada}
    // Consulta la factura de una entrada (datos enriquecidos)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/entrada/{idEntrada}")
    public ResponseEntity<?> obtenerPorEntrada(@PathVariable Long idEntrada) {
        try {
            Optional<FacturaEntrada> opt = facturaEntradaServiceAPI.obtenerFacturaPorEntrada(idEntrada);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No existe factura para la entrada: " + idEntrada));
            }
            FacturaDTO dto = facturaEntradaServiceAPI.construirDTO(opt.get());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.severe("Error consultando factura por entrada: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno al consultar la factura."));
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET /factura/{id}
    // Consulta una factura por su ID
    // ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            FacturaEntrada factura = facturaEntradaServiceAPI.get(id);
            if (factura == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Factura no encontrada: " + id));
            }
            FacturaDTO dto = facturaEntradaServiceAPI.construirDTO(factura);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.severe("Error consultando factura: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno al consultar la factura."));
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET /factura/getAll
    // Lista todas las facturas (uso admin / backoffice)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(facturaEntradaServiceAPI.getAll());
    }

    // ─────────────────────────────────────────────────────────
    // GET /factura/pdf/{idFactura}
    // Descarga el PDF de la factura como archivo adjunto
    // ─────────────────────────────────────────────────────────
    @GetMapping("/pdf/{idFactura}")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long idFactura) {
        try {
            logger.info("Solicitud de descarga PDF para factura: " + idFactura);
            byte[] pdfBytes = facturaEntradaServiceAPI.generarPdfBytes(idFactura);

            FacturaEntrada factura = facturaEntradaServiceAPI.get(idFactura);
            String nombreArchivo = (factura != null ? factura.getNumeroFactura() : "factura") + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.severe("Error generando PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─────────────────────────────────────────────────────────
    // POST /factura/enviar/{idFactura}
    // Envia la factura al correo del titular con el PDF adjunto
    // ─────────────────────────────────────────────────────────
    @PostMapping("/enviar/{idFactura}")
    public ResponseEntity<?> enviarPorCorreo(@PathVariable Long idFactura) {
        try {
            logger.info("Solicitud de envio por correo para factura: " + idFactura);
            facturaEntradaServiceAPI.enviarFacturaCorreo(idFactura);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Factura enviada exitosamente al correo del titular.",
                "idFactura", idFactura
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error enviando factura por correo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno al enviar la factura."));
        }
    }
}

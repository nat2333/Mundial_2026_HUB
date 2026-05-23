package co.edu.unbosque.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.dto.FacturaDTO;
import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.FacturaEntrada;
import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.repository.FacturaEntradaRepository;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.service.api.FacturaEntradaServiceAPI;
import co.edu.unbosque.service.api.PartidoServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.service.pdf.FacturaPdfRenderer;
import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.utils.Utilidad;

@Service
public class FacturaEntradaServiceImpl extends GenericServiceImpl<FacturaEntrada, Long>
        implements FacturaEntradaServiceAPI {

    private static final Logger logger = Logger.getLogger(FacturaEntradaServiceImpl.class.getName());
    private static final double IVA = 0.19;

    @Autowired private FacturaEntradaRepository facturaEntradaRepository;
    @Autowired private EntradaServiceAPI         entradaServiceAPI;
    @Autowired private UsuarioServiceAPI          usuarioServiceAPI;
    @Autowired private PartidoServiceAPI          partidoServiceAPI;
    @Autowired private EmailService               emailService;
    @Autowired private FacturaPdfRenderer         pdfRenderer;

    @Override
    public CrudRepository<FacturaEntrada, Long> getDao() {
        return facturaEntradaRepository;
    }

    // ── generarFactura: idempotente ───────────────────────────────────────────

    @Transactional
    @Override
    public FacturaEntrada generarFactura(Long idEntrada) {
        Optional<FacturaEntrada> existente = facturaEntradaRepository.findByIdEntrada(idEntrada);
        if (existente.isPresent()) {
            logger.info("Factura ya existe para entrada " + idEntrada + " — retornando existente.");
            return existente.get();
        }

        Entrada entrada = entradaServiceAPI.get(idEntrada);
        if (entrada == null) {
            throw new IllegalArgumentException("Entrada no encontrada: " + idEntrada);
        }
        if (!"PAGADA".equals(entrada.getEstado())) {
            throw new IllegalStateException(
                "Solo se puede facturar entradas en estado PAGADA. Estado actual: " + entrada.getEstado());
        }

        double precio    = entrada.getPrecio() != null ? entrada.getPrecio() : 0.0;
        BigDecimal total     = BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal  = BigDecimal.valueOf(precio / (1 + IVA)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuestos = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        String fechaStr      = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String sufijo        = Utilidad.generarIdCorrelacion().replace("-", "").substring(0, 8).toUpperCase();
        String numeroFactura = "FACT-2026-" + fechaStr + "-" + sufijo;

        FacturaEntrada factura = new FacturaEntrada();
        factura.setIdEntrada(idEntrada);
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaEmision(new Date());
        factura.setSubtotal(subtotal);
        factura.setImpuestos(impuestos);
        factura.setTotal(total);
        factura.setEnviadaCorreo((short) 0);

        FacturaEntrada guardada = facturaEntradaRepository.save(factura);
        logger.info("Factura generada: " + numeroFactura + " para entrada: " + idEntrada);
        return guardada;
    }

    @Override
    public Optional<FacturaEntrada> obtenerFacturaPorEntrada(Long idEntrada) {
        return facturaEntradaRepository.findByIdEntrada(idEntrada);
    }

    // ── construirDTO: ensambla respuesta enriquecida ─────────────────────────

    @Transactional(readOnly = true)
    @Override
    public FacturaDTO construirDTO(FacturaEntrada factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        dto.setIdEntrada(factura.getIdEntrada());
        dto.setNumeroFactura(factura.getNumeroFactura());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setSubtotal(factura.getSubtotal());
        dto.setImpuestos(factura.getImpuestos());
        dto.setTotal(factura.getTotal());
        dto.setEnviadaCorreo(factura.getEnviadaCorreo());
        dto.setFechaEnvio(factura.getFechaEnvio());

        Entrada entrada = entradaServiceAPI.get(factura.getIdEntrada());
        if (entrada != null) {
            dto.setTribuna(entrada.getTribuna());
            dto.setEstadoEntrada(entrada.getEstado());
            dto.setIdTransaccionPago(entrada.getIdTransaccionPago());
            dto.setIdCorrelacion(entrada.getIdCorrelacion());

            if (entrada.getIdTitular() != null) {
                Usuario usuario = usuarioServiceAPI.get(entrada.getIdTitular());
                if (usuario != null) {
                    dto.setNombreTitular(usuario.getNombres() + " " + usuario.getApellidos());
                    dto.setCorreoTitular(usuario.getCorreoUsuario());
                }
            }

            if (entrada.getIdPartido() != null) {
                Partido partido = partidoServiceAPI.get(entrada.getIdPartido());
                if (partido != null) {
                    dto.setEquipoLocal(partido.getEquipoLocal());
                    dto.setEquipoVisitante(partido.getEquipoVisitante());
                    dto.setFechaPartido(partido.getFechaHora());
                    dto.setEstadio(partido.getSede().getNombreEstadio());
                    dto.setCiudad(partido.getSede().getCiudad());
                    dto.setFase(partido.getFase());
                }
            }
        }
        return dto;
    }

    // ── generarPdfBytes: delega al renderer ──────────────────────────────────

    @Override
    public byte[] generarPdfBytes(Long idFactura) {
        FacturaEntrada factura = get(idFactura);
        if (factura == null) {
            throw new IllegalArgumentException("Factura no encontrada: " + idFactura);
        }
        Entrada entrada = entradaServiceAPI.get(factura.getIdEntrada());
        if (entrada == null) {
            throw new IllegalStateException("Entrada no encontrada para factura: " + idFactura);
        }
        Usuario usuario = entrada.getIdTitular() != null ? usuarioServiceAPI.get(entrada.getIdTitular()) : null;
        Partido partido = entrada.getIdPartido() != null ? partidoServiceAPI.get(entrada.getIdPartido()) : null;

        return pdfRenderer.renderizar(factura, entrada, usuario, partido);
    }

    // ── enviarFacturaCorreo ───────────────────────────────────────────────────

    @Transactional
    @Override
    public void enviarFacturaCorreo(Long idFactura) {
        FacturaEntrada factura = get(idFactura);
        if (factura == null) {
            throw new IllegalArgumentException("Factura no encontrada: " + idFactura);
        }
        Entrada entrada = entradaServiceAPI.get(factura.getIdEntrada());
        if (entrada == null) {
            throw new IllegalStateException("Entrada no encontrada para factura: " + idFactura);
        }
        Usuario usuario = entrada.getIdTitular() != null ? usuarioServiceAPI.get(entrada.getIdTitular()) : null;
        if (usuario == null) {
            throw new IllegalStateException("Titular no encontrado para la entrada: " + factura.getIdEntrada());
        }
        Partido partido = entrada.getIdPartido() != null ? partidoServiceAPI.get(entrada.getIdPartido()) : null;

        byte[] pdfBytes       = pdfRenderer.renderizar(factura, entrada, usuario, partido);
        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();

        emailService.enviarFacturaConAdjunto(
            usuario.getCorreoUsuario(),
            nombreCompleto,
            factura.getNumeroFactura(),
            pdfBytes
        );

        factura.setEnviadaCorreo((short) 1);
        factura.setFechaEnvio(new Date());
        facturaEntradaRepository.save(factura);

        logger.info("Factura " + factura.getNumeroFactura() + " enviada a: " + usuario.getCorreoUsuario());
    }
}

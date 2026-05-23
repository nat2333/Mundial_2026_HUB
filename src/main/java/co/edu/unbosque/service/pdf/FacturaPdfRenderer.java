package co.edu.unbosque.service.pdf;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.entity.FacturaEntrada;
import co.edu.unbosque.entity.Partido;
import co.edu.unbosque.entity.Usuario;

/**
 * Responsabilidad única: convertir los datos de una factura en bytes PDF.
 * No persiste, no valida reglas de negocio, no envía correos.
 */
@Component
public class FacturaPdfRenderer {

    private static final Logger logger = Logger.getLogger(FacturaPdfRenderer.class.getName());

    private static final Color COLOR_ROJO      = new Color(48, 79, 254);   // cobalt-blue (#304ffe)
    private static final Color COLOR_ROJO_DARK = new Color(30, 55, 200);
    private static final Color COLOR_VERDE     = new Color(0, 120, 0);
    private static final Color COLOR_VERDE_BG  = new Color(220, 255, 220);
    private static final Color COLOR_GRIS_BG   = new Color(248, 248, 248);
    private static final Color COLOR_TEXTO     = new Color(40, 40, 40);
    private static final Color COLOR_LABEL     = new Color(80, 80, 80);
    private static final Color COLOR_HEADER_BG = new Color(10, 14, 26);    // #0a0e1a

    public byte[] renderizar(FacturaEntrada factura, Entrada entrada,
                              Usuario usuario, Partido partido) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 50f, 50f, 60f, 60f);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            SimpleDateFormat sdf      = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");

            // ── Fuentes ──
            Font fTitulo     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   22, COLOR_ROJO);
            Font fSubtit     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   11, COLOR_ROJO);
            Font fSeccion    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   11, COLOR_ROJO);
            Font fLabel      = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10, COLOR_LABEL);
            Font fValor      = FontFactory.getFont(FontFactory.HELVETICA,        10, COLOR_TEXTO);
            Font fTotalBlanco = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, Color.WHITE);
            Font fPagada     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   14, COLOR_VERDE);
            Font fRef        = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(160, 160, 160));
            Font fFooter     = FontFactory.getFont(FontFactory.HELVETICA,         8, new Color(160, 160, 160));

            // ── CABECERA ─────────────────────────────────────────
            PdfPTable cabecera = new PdfPTable(1);
            cabecera.setWidthPercentage(100);
            cabecera.setSpacingAfter(10f);

            PdfPCell celdaCab = new PdfPCell();
            celdaCab.setBackgroundColor(COLOR_HEADER_BG);
            celdaCab.setPadding(20f);
            celdaCab.setBorder(Rectangle.NO_BORDER);

            Paragraph titulo = new Paragraph("MUNDIAL 2026 HUB", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            Paragraph subtit = new Paragraph("COMPROBANTE OFICIAL DE COMPRA - ENTRADA", fSubtit);
            subtit.setAlignment(Element.ALIGN_CENTER);

            celdaCab.addElement(titulo);
            celdaCab.addElement(subtit);
            cabecera.addCell(celdaCab);
            doc.add(cabecera);

            // ── NUMERO Y FECHA ────────────────────────────────────
            PdfPTable infoFact = new PdfPTable(new float[]{50, 50});
            infoFact.setWidthPercentage(100);
            infoFact.setSpacingBefore(6f);
            infoFact.setSpacingAfter(12f);
            agregarPar(infoFact, "N.° Factura:",    factura.getNumeroFactura(),             fLabel, fValor);
            agregarPar(infoFact, "Fecha Emisión:",  sdf.format(factura.getFechaEmision()),   fLabel, fValor);
            doc.add(infoFact);

            // ── TITULAR ───────────────────────────────────────────
            doc.add(new Paragraph("DATOS DEL TITULAR", fSeccion));
            doc.add(new LineSeparator(0.8f, 100f, COLOR_ROJO, Element.ALIGN_LEFT, -2f));
            doc.add(new Paragraph(" "));

            PdfPTable datTitular = new PdfPTable(new float[]{35, 65});
            datTitular.setWidthPercentage(100);
            datTitular.setSpacingAfter(12f);

            String nombre = usuario != null ? usuario.getNombres() + " " + usuario.getApellidos() : "N/A";
            String correo = usuario != null ? usuario.getCorreoUsuario() : "N/A";
            agregarPar(datTitular, "Nombre:", nombre, fLabel, fValor);
            agregarPar(datTitular, "Correo:", correo, fLabel, fValor);
            doc.add(datTitular);

            // ── PARTIDO ───────────────────────────────────────────
            doc.add(new Paragraph("DETALLES DEL PARTIDO", fSeccion));
            doc.add(new LineSeparator(0.8f, 100f, COLOR_ROJO, Element.ALIGN_LEFT, -2f));
            doc.add(new Paragraph(" "));

            PdfPTable datPartido = new PdfPTable(new float[]{35, 65});
            datPartido.setWidthPercentage(100);
            datPartido.setSpacingAfter(12f);

            String equipos  = partido != null ? partido.getEquipoLocal() + " vs " + partido.getEquipoVisitante() : "N/A";
            String fPartido = partido != null && partido.getFechaHora() != null ? sdf.format(partido.getFechaHora()) : "N/A";
            String estadio  = nvl(partido != null && partido.getSede() != null ? partido.getSede().getNombreEstadio() : null);
            String ciudad   = nvl(partido != null && partido.getSede() != null ? partido.getSede().getCiudad() : null);
            String fase     = nvl(partido != null ? partido.getFase() : null);

            agregarPar(datPartido, "Partido:",  equipos,                   fLabel, fValor);
            agregarPar(datPartido, "Fecha:",    fPartido,                  fLabel, fValor);
            agregarPar(datPartido, "Estadio:",  estadio,                   fLabel, fValor);
            agregarPar(datPartido, "Ciudad:",   ciudad,                    fLabel, fValor);
            agregarPar(datPartido, "Fase:",     fase,                      fLabel, fValor);
            agregarPar(datPartido, "Tribuna:",  nvl(entrada.getTribuna()), fLabel, fValor);
            doc.add(datPartido);

            // ── RESUMEN DE PAGO ───────────────────────────────────
            doc.add(new Paragraph("RESUMEN DE PAGO", fSeccion));
            doc.add(new LineSeparator(0.8f, 100f, COLOR_ROJO, Element.ALIGN_LEFT, -2f));
            doc.add(new Paragraph(" "));

            PdfPTable tablaPago = new PdfPTable(new float[]{60, 40});
            tablaPago.setWidthPercentage(55);
            tablaPago.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaPago.setSpacingAfter(10f);

            agregarFilaPago(tablaPago, "Subtotal (sin IVA):", "$ " + factura.getSubtotal(), fLabel, fValor, COLOR_GRIS_BG);
            agregarFilaPago(tablaPago, "IVA (19%):",          "$ " + factura.getImpuestos(), fLabel, fValor, Color.WHITE);
            agregarFilaTotal(tablaPago, "TOTAL PAGADO:",      "$ " + factura.getTotal(),    fTotalBlanco);
            doc.add(tablaPago);

            // ── ESTADO PAGADA ─────────────────────────────────────
            PdfPTable estadoTab = new PdfPTable(1);
            estadoTab.setWidthPercentage(38);
            estadoTab.setHorizontalAlignment(Element.ALIGN_CENTER);
            estadoTab.setSpacingAfter(14f);

            PdfPCell celdaEstado = new PdfPCell(new Paragraph("PAGADA", fPagada));
            celdaEstado.setBackgroundColor(COLOR_VERDE_BG);
            celdaEstado.setBorderColor(COLOR_VERDE);
            celdaEstado.setBorderWidth(1.5f);
            celdaEstado.setPadding(10f);
            celdaEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
            estadoTab.addCell(celdaEstado);
            doc.add(estadoTab);

            // ── REFERENCIA INTERNA ────────────────────────────────
            Paragraph ref = new Paragraph(
                "Correlación: " + nvl(entrada.getIdCorrelacion())
                + "   |   Transacción: " + nvl(entrada.getIdTransaccionPago())
                + "   |   ID Entrada: " + entrada.getId(), fRef);
            ref.setAlignment(Element.ALIGN_CENTER);
            doc.add(ref);

            // ── PIE DE PÁGINA ─────────────────────────────────────
            doc.add(new Paragraph(" "));
            PdfPTable pie = new PdfPTable(1);
            pie.setWidthPercentage(100);

            PdfPCell celdaPie = new PdfPCell();
            celdaPie.setBackgroundColor(COLOR_HEADER_BG);
            celdaPie.setPadding(12f);
            celdaPie.setBorder(Rectangle.NO_BORDER);

            Paragraph txtPie = new Paragraph(
                "© 2026 Mundial Hub · Universidad El Bosque  |  "
                + "Emitido: " + sdfFecha.format(factura.getFechaEmision())
                + "  |  Documento oficial no fiscal", fFooter);
            txtPie.setAlignment(Element.ALIGN_CENTER);
            celdaPie.addElement(txtPie);
            pie.addCell(celdaPie);
            doc.add(pie);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            logger.severe("Error generando PDF para factura " + factura.getNumeroFactura() + ": " + e.getMessage());
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void agregarPar(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPaddingBottom(5f);
        cLabel.setPaddingLeft(2f);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fValor));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setPaddingBottom(5f);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }

    private void agregarFilaPago(PdfPTable tabla, String label, String valor,
                                  Font fLabel, Font fValor, Color bg) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBackgroundColor(bg);
        cLabel.setPadding(7f);
        cLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fValor));
        cValor.setBackgroundColor(bg);
        cValor.setPadding(7f);
        cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }

    private void agregarFilaTotal(PdfPTable tabla, String label, String valor, Font font) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, font));
        cLabel.setBackgroundColor(COLOR_ROJO);
        cLabel.setPadding(10f);
        cLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, font));
        cValor.setBackgroundColor(COLOR_ROJO);
        cValor.setPadding(10f);
        cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }

    private String nvl(String val) {
        return (val != null && !val.isBlank()) ? val : "N/A";
    }
}

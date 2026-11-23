package pe.com.bonapharm.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import pe.com.bonapharm.entity.*;

@Service
public class GuiaPdfService {

    @Value("${bonapharm.empresa.nombre:Bonapharm}")
    private String empresaNombre;
    @Value("${bonapharm.empresa.direccion:}")
    private String empresaDireccion;
    @Value("${bonapharm.empresa.punto-partida:}")
    private String puntoPartida;

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TIT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    private static final Font B = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font N = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private PdfPCell c(String t, Font f, int align, boolean borde) {
        PdfPCell x = new PdfPCell(new Phrase(t, f));
        x.setHorizontalAlignment(align); x.setPadding(5); x.setMinimumHeight(20);
        if (!borde) x.setBorder(Rectangle.NO_BORDER);
        return x;
    }

    private String nz(String s) { return s == null || s.isBlank() ? " " : s; }

    private void fila(PdfPTable t, String a, String b) { t.addCell(c(a, B, Element.ALIGN_LEFT, true)); t.addCell(c(b, N, Element.ALIGN_LEFT, true)); }

    public byte[] generar(VentasEntity v) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document d = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(d, out);
            d.open();

            PdfPTable cab = new PdfPTable(new float[]{58, 42});
            cab.setWidthPercentage(100); cab.setSpacingAfter(12);
            PdfPCell emp = new PdfPCell(); emp.setBorder(Rectangle.NO_BORDER);
            emp.addElement(new Paragraph(empresaNombre, TIT));
            emp.addElement(new Paragraph(nz(empresaDireccion), N));
            cab.addCell(emp);
            PdfPCell num = new PdfPCell(); num.setPadding(8);
            Paragraph p1 = new Paragraph("GUÍA DE REMISIÓN REMITENTE", B); p1.setAlignment(Element.ALIGN_CENTER);
            Paragraph p2 = new Paragraph("N.º " + v.getNumeroGuia(), TIT); p2.setAlignment(Element.ALIGN_CENTER);
            num.addElement(p1); num.addElement(p2);
            cab.addCell(num);
            d.add(cab);

            PdfPTable fe = new PdfPTable(4);
            fe.setWidthPercentage(100); fe.setSpacingAfter(8);
            fe.addCell(c("Fecha de emisión:", B, Element.ALIGN_LEFT, true)); fe.addCell(c(v.getFechaEmision().format(F), N, Element.ALIGN_LEFT, true));
            fe.addCell(c("Fecha de traslado:", B, Element.ALIGN_LEFT, true)); fe.addCell(c(v.getFechaTraslado().format(F), N, Element.ALIGN_LEFT, true));
            d.add(fe);

            PdfPTable ds = new PdfPTable(new float[]{25, 75});
            ds.setWidthPercentage(100); ds.setSpacingAfter(8);
            fila(ds, "Destinatario:", nz(v.getDestinatario()));
            fila(ds, "RUC:", nz(v.getRucDestinatario()));
            fila(ds, "Dirección:", nz(v.getDireccionDestinatario()));
            fila(ds, "Punto de partida:", nz(puntoPartida));
            fila(ds, "Punto de llegada:", nz(v.getPuntoLlegada()));
            d.add(ds);

            PdfPTable det = new PdfPTable(new float[]{7, 16, 33, 14, 16, 14});
            det.setWidthPercentage(100); det.setSpacingAfter(10);
            for (String h : new String[]{"Ítem", "Código", "Producto", "Lote", "F. vencimiento", "Cantidad"}) det.addCell(c(h, B, Element.ALIGN_CENTER, true));
            int i = 1;
            for (DetalleVentasEntity x : v.getDetalles()) {
                det.addCell(c(String.valueOf(i++), N, Element.ALIGN_CENTER, true));
                det.addCell(c(x.getProducto().getCodigo(), N, Element.ALIGN_LEFT, true));
                det.addCell(c(x.getProducto().getNombre(), N, Element.ALIGN_LEFT, true));
                det.addCell(c(x.getLote().getCodigoLote(), N, Element.ALIGN_CENTER, true));
                det.addCell(c(x.getLote().getFechaVencimiento().format(F), N, Element.ALIGN_CENTER, true));
                det.addCell(c(String.valueOf(x.getCantidad()), N, Element.ALIGN_CENTER, true));
            }
            d.add(det);

            if ("CANCELADA".equals(v.getEstado())) {
                Paragraph an = new Paragraph("\nVENTA CANCELADA - DOCUMENTO ANULADO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.RED));
                an.setAlignment(Element.ALIGN_CENTER);
                d.add(an);
            }
            d.close();
            return out.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("No se pudo generar el PDF.", e); }
    }
}

package pe.com.bonapharm.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class ReportePdfService {

    private static final Font TIT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font B = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font N = FontFactory.getFont(FontFactory.HELVETICA, 8);

    private PdfPCell c(String t, Font f, boolean head) {
        PdfPCell x = new PdfPCell(new Phrase(t, f));
        x.setPadding(4);
        if (head) x.setBackgroundColor(new java.awt.Color(217, 226, 243));
        return x;
    }

    public byte[] generar(Reporte r) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document d = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(d, out);
            d.open();
            d.add(new Paragraph("BONAPHARM SAC - " + r.getTitulo(), TIT));
            d.add(new Paragraph(r.getFiltros() + "   |   Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), N));
            d.add(new Paragraph(" "));
            if (r.getFilas().isEmpty()) {
                d.add(new Paragraph(r.getMensajeVacio(), N));
            } else {
                PdfPTable tot = new PdfPTable(2);
                tot.setWidthPercentage(45); tot.setHorizontalAlignment(Element.ALIGN_LEFT); tot.setSpacingAfter(8);
                for (String[] t : r.getTotales()) { tot.addCell(c(t[0], B, true)); tot.addCell(c(t[1], N, false)); }
                d.add(tot);
                PdfPTable tb = new PdfPTable(r.getColumnas().size());
                tb.setWidthPercentage(100);
                tb.setHeaderRows(1);
                for (String h : r.getColumnas()) tb.addCell(c(h, B, true));
                for (var fila : r.getFilas()) for (String v : fila) tb.addCell(c(v, N, false));
                d.add(tb);
                if (r.tieneDetalle()) {
                    d.add(new Paragraph(" "));
                    d.add(new Paragraph(r.getTituloDetalle(), TIT));
                    d.add(new Paragraph(" "));
                    PdfPTable td = new PdfPTable(r.getColumnasDetalle().size());
                    td.setWidthPercentage(100);
                    td.setHeaderRows(1);
                    for (String h : r.getColumnasDetalle()) td.addCell(c(h, B, true));
                    for (var fila : r.getFilasDetalle()) for (String v : fila) td.addCell(c(v, N, false));
                    d.add(td);
                }
            }
            d.close();
            return out.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("No se pudo generar el PDF.", e); }
    }
}

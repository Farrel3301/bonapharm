package pe.com.bonapharm.service;

import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ReporteExcelService {

    // Los importes y cantidades se escriben como numeros; codigos, RUC y fechas como texto
    private void put(Row row, int col, String v, CellStyle st) {
        Cell c = row.createCell(col);
        if (v.matches("-?\\d{1,9}(\\.\\d{1,2})?")) c.setCellValue(Double.parseDouble(v)); else c.setCellValue(v);
        if (st != null) c.setCellStyle(st);
    }

    public byte[] generar(Reporte r) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Reporte");
            Font bold = wb.createFont(); bold.setBold(true);
            CellStyle head = wb.createCellStyle();
            head.setFont(bold); head.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle b = wb.createCellStyle(); b.setFont(bold);
            int i = 0;
            put(sh.createRow(i++), 0, "BONAPHARM SAC - " + r.getTitulo(), b);
            put(sh.createRow(i++), 0, r.getFiltros(), null);
            i++;
            if (r.getFilas().isEmpty()) {
                put(sh.createRow(i), 0, r.getMensajeVacio(), null);
            } else {
                for (String[] t : r.getTotales()) { Row row = sh.createRow(i++); put(row, 0, t[0], b); put(row, 1, t[1], null); }
                i++;
                Row h = sh.createRow(i++);
                for (int c = 0; c < r.getColumnas().size(); c++) put(h, c, r.getColumnas().get(c), head);
                for (var fila : r.getFilas()) { Row row = sh.createRow(i++); for (int c = 0; c < fila.size(); c++) put(row, c, fila.get(c), null); }
                for (int c = 0; c < r.getColumnas().size(); c++) sh.autoSizeColumn(c);
            }
            if (r.tieneDetalle()) {
                Sheet s2 = wb.createSheet("Detalle por lote");
                Row h2 = s2.createRow(0);
                for (int c = 0; c < r.getColumnasDetalle().size(); c++) put(h2, c, r.getColumnasDetalle().get(c), head);
                int k = 1;
                for (var fila : r.getFilasDetalle()) { Row row = s2.createRow(k++); for (int c = 0; c < fila.size(); c++) put(row, c, fila.get(c), null); }
                for (int c = 0; c < r.getColumnasDetalle().size(); c++) s2.autoSizeColumn(c);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("No se pudo generar el Excel.", e); }
    }
}

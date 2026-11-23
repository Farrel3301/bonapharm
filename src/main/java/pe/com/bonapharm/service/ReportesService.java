package pe.com.bonapharm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.bonapharm.entity.*;
import pe.com.bonapharm.repository.*;

@Service
public class ReportesService {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @Autowired private ProductosRepository productos;
    @Autowired private LotesRepository lotes;
    @Autowired private VentasRepository ventas;

    private String money(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
    private String[] t(String a, String b) { return new String[]{a, b}; }

    // HU-16 Reporte de inventario
    public Reporte inventario(String categoria, String estado) {
        List<List<String>> filas = new ArrayList<>();
        List<List<String>> detalle = new ArrayList<>();
        int unidades = 0, vencidas = 0, alerta = 0;
        BigDecimal valor = BigDecimal.ZERO;
        for (ProductosEntity p : productos.findAll()) {
            if (!Boolean.TRUE.equals(p.getEstado())) continue;
            if (!categoria.isBlank() && !p.getCategoria().equals(categoria)) continue;
            int vend = 0, venc = 0;
            List<List<String>> detalleP = new ArrayList<>();
            for (LotesEntity l : lotes.findByProducto_IdAndCantidadGreaterThanOrderByFechaVencimientoAsc(p.getId(), 0)) {
                long dias = l.getDiasParaVencer();
                if (dias < 0) venc += l.getCantidad(); else vend += l.getCantidad();
                detalleP.add(List.of(p.getCodigo(), p.getNombre(), l.getCodigoLote(), l.getFechaVencimiento().format(F), String.valueOf(dias),
                        String.valueOf(l.getCantidad()), money(p.getPrecio().multiply(BigDecimal.valueOf(l.getCantidad()))),
                        dias < 0 ? "Vencido" : (dias <= 30 ? "Por vencer" : "Vigente")));
            }
            String est = vend == 0 ? "Agotado" : (vend <= p.getStockMinimo() ? "Stock bajo" : "Disponible");
            if (!estado.isBlank() && !est.equals(estado)) continue;
            BigDecimal val = p.getPrecio().multiply(BigDecimal.valueOf(vend));
            filas.add(List.of(p.getCodigo(), p.getNombre(), p.getCategoria(), String.valueOf(p.getStock()), String.valueOf(vend),
                    String.valueOf(venc), String.valueOf(p.getStockMinimo()), money(val), est));
            unidades += vend; vencidas += venc; valor = valor.add(val);
            detalle.addAll(detalleP);
            if (!est.equals("Disponible")) alerta++;
        }
        List<String[]> tot = List.of(t("Productos", String.valueOf(filas.size())), t("Unidades vendibles", String.valueOf(unidades)),
                t("Unidades vencidas", String.valueOf(vencidas)), t("Valor del inventario (S/)", money(valor)), t("Con stock bajo o agotados", String.valueOf(alerta)));
        return new Reporte("Reporte de inventario", "Categoría: " + (categoria.isBlank() ? "Todas" : categoria) + " | Estado: " + (estado.isBlank() ? "Todos" : estado),
                List.of("Código", "Producto", "Categoría", "Stock total", "Vendible", "Vencido", "Stock mín.", "Valor vendible (S/)", "Estado"),
                filas, tot, "No existen registros de inventario para los filtros indicados.",
                "Detalle por lote", List.of("Código", "Producto", "Lote", "Vence", "Días restantes", "Cantidad", "Valor (S/)", "Estado del lote"), detalle);
    }

    // HU-17 Reporte de ventas por periodo
    public Reporte ventas(LocalDate desde, LocalDate hasta, Long clienteId, String vendedor) {
        List<List<String>> filas = new ArrayList<>();
        BigDecimal sub = BigDecimal.ZERO, igv = BigDecimal.ZERO, total = BigDecimal.ZERO;
        int activas = 0, canceladas = 0;
        Map<String, Integer> unid = new HashMap<>();
        Map<String, BigDecimal> porCliente = new HashMap<>();
        for (VentasEntity v : ventas.findAllByOrderByFechaDesc()) {
            if (v.getFechaEmision().isBefore(desde) || v.getFechaEmision().isAfter(hasta)) continue;
            if (clienteId != null && !v.getCliente().getId().equals(clienteId)) continue;
            if (!vendedor.isBlank() && !v.getUsuario().equals(vendedor)) continue;
            boolean ok = "ACTIVA".equals(v.getEstado());
            filas.add(List.of(v.getNumeroGuia(), v.getFechaEmision().format(F), v.getDestinatario(), v.getRucDestinatario(), v.getUsuario(),
                    money(v.getSubtotal()), money(v.getIgv()), money(v.getTotal()), ok ? "Activa" : "Cancelada"));
            if (!ok) { canceladas++; continue; }
            activas++; sub = sub.add(v.getSubtotal()); igv = igv.add(v.getIgv()); total = total.add(v.getTotal());
            porCliente.merge(v.getDestinatario(), v.getTotal(), BigDecimal::add);
            for (DetalleVentasEntity d : v.getDetalles()) unid.merge(d.getProducto().getNombre(), d.getCantidad(), Integer::sum);
        }
        List<String[]> tot = new ArrayList<>(List.of(t("Ventas realizadas", String.valueOf(activas)), t("Subtotal (S/)", money(sub)),
                t("IGV 18% (S/)", money(igv)), t("Total vendido (S/)", money(total)), t("Ventas canceladas (no suman)", String.valueOf(canceladas))));
        unid.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(e -> tot.add(t("Producto más vendido", e.getKey() + " (" + e.getValue() + " und.)")));
        porCliente.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(e -> tot.add(t("Cliente que más compró", e.getKey() + " (S/ " + money(e.getValue()) + ")")));
        return new Reporte("Reporte de ventas", "Período: " + desde.format(F) + " al " + hasta.format(F),
                List.of("Guía N.º", "Emisión", "Cliente", "RUC", "Vendedor", "Subtotal (S/)", "IGV (S/)", "Total (S/)", "Estado"),
                filas, tot, "No existen ventas para el período indicado.");
    }

    // HU-18 Reporte de productos proximos a vencer
    public Reporte vencimientos(int dias, boolean incluirVencidos) {
        List<List<String>> filas = new ArrayList<>();
        int unidades = 0, vencidos = 0;
        BigDecimal riesgo = BigDecimal.ZERO;
        for (LotesEntity l : lotes.findByCantidadGreaterThanAndFechaVencimientoLessThanEqualOrderByFechaVencimientoAsc(0, LocalDate.now().plusDays(dias))) {
            long d = l.getDiasParaVencer();
            if (d < 0 && !incluirVencidos) continue;
            BigDecimal val = l.getProducto().getPrecio().multiply(BigDecimal.valueOf(l.getCantidad()));
            filas.add(List.of(l.getProducto().getCodigo(), l.getProducto().getNombre(), l.getCodigoLote(), l.getFechaVencimiento().format(F),
                    String.valueOf(d), String.valueOf(l.getCantidad()), money(val), d < 0 ? "Vencido" : "Por vencer"));
            unidades += l.getCantidad(); riesgo = riesgo.add(val);
            if (d < 0) vencidos++;
        }
        List<String[]> tot = List.of(t("Lotes", String.valueOf(filas.size())), t("Unidades", String.valueOf(unidades)),
                t("Valor en riesgo (S/)", money(riesgo)), t("Lotes ya vencidos", String.valueOf(vencidos)));
        return new Reporte("Reporte de productos próximos a vencer", "Rango: próximos " + dias + " días | " + (incluirVencidos ? "Incluye vencidos" : "Sin vencidos"),
                List.of("Código", "Producto", "Lote", "Vence", "Días restantes", "Cantidad", "Valor en riesgo (S/)", "Estado"),
                filas, tot, "No existen productos dentro del rango definido.");
    }
}

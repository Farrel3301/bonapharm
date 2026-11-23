package pe.com.bonapharm.controller;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.com.bonapharm.service.*;

// HU-16, HU-17, HU-18: reportes solo para ADMIN (el interceptor bloquea al vendedor)
@Controller
@RequestMapping("/reportes")
public class ReportesController {

    @Autowired private ReportesService reportes;
    @Autowired private ReportePdfService pdf;
    @Autowired private ReporteExcelService excel;
    @Autowired private CatalogoService catalogo;
    @Autowired private ClientesService clientes;
    @Autowired private UsuariosService usuarios;

    private ResponseEntity<byte[]> descarga(Reporte r, String fmt, String nombre) {
        boolean esPdf = fmt.equals("pdf");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + (esPdf ? ".pdf" : ".xlsx") + "\"")
                .contentType(esPdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(esPdf ? pdf.generar(r) : excel.generar(r));
    }

    // ---- HU-16 Inventario
    @GetMapping("/inventario")
    public String MostrarInventario(@RequestParam(defaultValue = "") String categoria, @RequestParam(defaultValue = "") String estado, Model m) {
        m.addAttribute("r", reportes.inventario(categoria, estado));
        m.addAttribute("categorias", catalogo.categorias());
        m.addAttribute("categoria", categoria);
        m.addAttribute("estado", estado);
        return "reportes/inventario";
    }

    @GetMapping("/inventario/{fmt}")
    public ResponseEntity<byte[]> DescargarInventario(@PathVariable String fmt, @RequestParam(defaultValue = "") String categoria, @RequestParam(defaultValue = "") String estado) {
        return descarga(reportes.inventario(categoria, estado), fmt, "reporte-inventario");
    }

    // ---- HU-17 Ventas
    @GetMapping("/ventas")
    public String MostrarVentas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long clienteId, @RequestParam(defaultValue = "") String vendedor, Model m) {
        LocalDate d = desde != null ? desde : LocalDate.now().withDayOfMonth(1), h = hasta != null ? hasta : LocalDate.now();
        m.addAttribute("desde", d.toString()); m.addAttribute("hasta", h.toString());
        m.addAttribute("clienteId", clienteId); m.addAttribute("vendedor", vendedor);
        m.addAttribute("clientes", clientes.listar()); m.addAttribute("usuarios", usuarios.listar());
        if (d.isAfter(h)) m.addAttribute("error", "La fecha inicial no puede ser posterior a la final.");
        else m.addAttribute("r", reportes.ventas(d, h, clienteId, vendedor));
        return "reportes/ventas";
    }

    @GetMapping("/ventas/{fmt}")
    public ResponseEntity<byte[]> DescargarVentas(@PathVariable String fmt, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long clienteId, @RequestParam(defaultValue = "") String vendedor) {
        LocalDate d = desde != null ? desde : LocalDate.now().withDayOfMonth(1), h = hasta != null ? hasta : LocalDate.now();
        if (d.isAfter(h)) return ResponseEntity.badRequest().build();
        return descarga(reportes.ventas(d, h, clienteId, vendedor), fmt, "reporte-ventas");
    }

    // ---- HU-18 Productos proximos a vencer
    @GetMapping("/vencimientos")
    public String MostrarVencimientos(@RequestParam(defaultValue = "60") int dias, @RequestParam(required = false) String enviado,
            @RequestParam(required = false) String incluir, Model m) {
        boolean inc = enviado == null || incluir != null;
        m.addAttribute("dias", dias); m.addAttribute("incluir", inc);
        m.addAttribute("r", reportes.vencimientos(dias, inc));
        return "reportes/vencimientos";
    }

    @GetMapping("/vencimientos/{fmt}")
    public ResponseEntity<byte[]> DescargarVencimientos(@PathVariable String fmt, @RequestParam(defaultValue = "60") int dias, @RequestParam(required = false) String incluir) {
        return descarga(reportes.vencimientos(dias, incluir != null), fmt, "reporte-vencimientos");
    }
}

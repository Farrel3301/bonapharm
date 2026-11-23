package pe.com.bonapharm.controller;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.bonapharm.entity.ProductosEntity;
import pe.com.bonapharm.service.*;

@Controller
@RequestMapping("/ventas")
public class VentasController {

    @Autowired private VentasService ventas;
    @Autowired private ProductosService productos;
    @Autowired private InventarioService inv;
    @Autowired private GuiaPdfService pdf;
    @Autowired private ClientesService clientes;

    private String usuario(HttpSession s) { return (String) s.getAttribute("usuario"); }
    private String rol(HttpSession s) { return (String) s.getAttribute("rol"); }

    private String vista(Model m, String error) {
        List<ProductosEntity> lista = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> mapa = new LinkedHashMap<>();
        for (ProductosEntity p : productos.buscar("")) {
            var ls = inv.lotesVendibles(p.getId());
            if (ls.isEmpty()) continue;
            lista.add(p);
            mapa.put(p.getId(), ls.stream().map(l -> Map.<String, Object>of("id", l.getId(), "codigo", l.getCodigoLote(),
                    "vence", l.getFechaVencimiento().toString(), "cantidad", l.getCantidad())).toList());
        }
        m.addAttribute("productos", lista);
        m.addAttribute("lotesMapa", mapa);
        m.addAttribute("hoy", LocalDate.now().toString());
        m.addAttribute("clientes", clientes.activos());
        if (error != null) m.addAttribute("error", error);
        return "ventas/venta";
    }

    @GetMapping("/registro")
    public String MostrarVenta(Model m) { return vista(m, null); }

    @PostMapping("/registrar")
    public String RegistrarVenta(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmision,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaTraslado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) List<Long> productoId, @RequestParam(required = false) List<Long> loteId,
            @RequestParam(required = false) List<Integer> cantidad, HttpSession s, Model m, RedirectAttributes ra) {
        try {
            var v = ventas.registrar(usuario(s), fechaEmision, fechaTraslado, clienteId, productoId, loteId, cantidad);
            ra.addFlashAttribute("ok", "Venta confirmada. Se generó la guía de remisión " + v.getNumeroGuia() + ". Total: S/ " + v.getTotal());
            ra.addFlashAttribute("ventaId", v.getId());
            return "redirect:/ventas/registro";
        } catch (IllegalArgumentException | IllegalStateException e) { return vista(m, e.getMessage()); }
        catch (Exception e) { return vista(m, "No se pudo completar la venta. No se descontó stock."); }   // HU-13, escenario 2
    }

    // HU-15 Consultar historial de compras de un cliente
    @GetMapping("/cliente")
    public String MostrarCompras(@RequestParam(required = false) Long clienteId, Model m) {
        m.addAttribute("clientes", clientes.listar());
        if (clienteId != null) {
            m.addAttribute("clienteId", clienteId);
            m.addAttribute("lista", ventas.comprasDeCliente(clienteId));
        }
        return "ventas/compras";
    }

    @GetMapping("/historial")
    public String MostrarHistorial(HttpSession s, Model m) {
        m.addAttribute("lista", ventas.listar(usuario(s), rol(s)));
        return "ventas/historial";
    }

    @PostMapping("/cancelar/{id}")
    public String CancelarVenta(@PathVariable Long id, HttpSession s, RedirectAttributes ra) {
        try {
            ventas.cancelar(id, usuario(s), rol(s));
            ra.addFlashAttribute("ok", "Venta cancelada. El stock fue devuelto a sus lotes.");
        } catch (IllegalArgumentException | IllegalStateException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/ventas/historial";
    }

    // Ver la guia en el navegador o descargarla (?descargar=true)
    @GetMapping("/guia/{id}")
    public ResponseEntity<byte[]> Guia(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean descargar, HttpSession s) {
        try {
            var v = ventas.obtener(id, usuario(s), rol(s));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, (descargar ? "attachment" : "inline") + "; filename=\"guia-" + v.getNumeroGuia() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF).body(pdf.generar(v));
        } catch (IllegalArgumentException e) { return ResponseEntity.notFound().build(); }
    }
}

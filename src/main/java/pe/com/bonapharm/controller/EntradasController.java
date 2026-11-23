package pe.com.bonapharm.controller;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.bonapharm.service.*;

@Controller
@RequestMapping("/entradas")
public class EntradasController {

    @Autowired private InventarioService inv;
    @Autowired private ProductosService productos;
    @Autowired private CatalogoService catalogo;

    private String vista(Model m, String error) {
        m.addAttribute("productos", productos.buscar(""));
        m.addAttribute("lotes", catalogo.lotes());
        if (error != null) m.addAttribute("error", error);
        return "inventario/entrada";
    }

    @GetMapping("/registro")
    public String MostrarEntrada(Model m) { return vista(m, null); }

    @PostMapping("/registrar")
    public String RegistrarEntrada(@RequestParam(required = false) Long productoId, @RequestParam(required = false) String codigoLote,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
            @RequestParam(required = false) Integer cantidad, @RequestParam(required = false) String guia,
            HttpSession s, Model m, RedirectAttributes ra) {
        if (productoId == null || codigoLote == null || codigoLote.isBlank() || fechaVencimiento == null || cantidad == null || guia == null || guia.isBlank())
            return vista(m, "Faltan datos requeridos: producto, lote, fecha de vencimiento, cantidad y guía de remisión.");
        if (cantidad <= 0) return vista(m, "La cantidad debe ser mayor a 0.");
        if (fechaVencimiento.isBefore(LocalDate.now())) return vista(m, "No se puede ingresar un lote ya vencido.");
        try {
            inv.registrarEntrada(productoId, codigoLote, fechaVencimiento, cantidad, guia.trim(), (String) s.getAttribute("usuario"));
        } catch (IllegalArgumentException e) { return vista(m, e.getMessage()); }
        ra.addFlashAttribute("ok", "Entrada registrada. El stock fue actualizado.");
        return "redirect:/entradas/registro";
    }
}

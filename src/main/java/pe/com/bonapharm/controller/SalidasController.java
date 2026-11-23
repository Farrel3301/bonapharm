package pe.com.bonapharm.controller;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.bonapharm.entity.ProductosEntity;
import pe.com.bonapharm.service.*;

// HU-06 Registrar salida de productos (vencidos, mermas, devoluciones, ajustes)
@Controller
@RequestMapping("/salidas")
public class SalidasController {

    @Autowired private InventarioService inv;
    @Autowired private ProductosService productos;
    @Autowired private CatalogoService catalogo;

    private String vista(Model m, String error, Long pid, Long lid) {
        List<ProductosEntity> lista = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> mapa = new LinkedHashMap<>();
        for (ProductosEntity p : productos.buscar("")) {
            var ls = inv.lotesConStock(p.getId());
            if (ls.isEmpty()) continue;
            lista.add(p);
            mapa.put(p.getId(), ls.stream().map(l -> Map.<String, Object>of("id", l.getId(), "codigo", l.getCodigoLote(),
                    "vence", l.getFechaVencimiento().toString(), "cantidad", l.getCantidad(), "vencido", l.getDiasParaVencer() < 0)).toList());
        }
        m.addAttribute("productos", lista);
        m.addAttribute("lotesMapa", mapa);
        m.addAttribute("motivos", catalogo.motivos());
        m.addAttribute("preProducto", pid == null ? 0 : pid);
        m.addAttribute("preLote", lid == null ? 0 : lid);
        if (error != null) m.addAttribute("error", error);
        return "inventario/salida";
    }

    @GetMapping("/registro")
    public String MostrarSalida(@RequestParam(required = false) Long productoId, @RequestParam(required = false) Long loteId, Model m) {
        return vista(m, null, productoId, loteId);
    }

    @PostMapping("/registrar")
    public String RegistrarSalida(@RequestParam(required = false) Long loteId, @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) String motivo, HttpSession s, Model m, RedirectAttributes ra) {
        if (loteId == null || cantidad == null || motivo == null || motivo.isBlank())
            return vista(m, "Faltan datos requeridos: producto, lote, cantidad y motivo.", null, null);
        if (cantidad <= 0) return vista(m, "La cantidad debe ser mayor a 0.", null, null);
        try {
            inv.registrarSalida(loteId, cantidad, motivo, (String) s.getAttribute("usuario"));
        } catch (IllegalArgumentException | IllegalStateException e) { return vista(m, e.getMessage(), null, null); }
        ra.addFlashAttribute("ok", "Salida registrada. El stock fue actualizado.");
        return "redirect:/salidas/registro";
    }
}

package pe.com.bonapharm.controller;

import pe.com.bonapharm.service.InventarioService;
import pe.com.bonapharm.service.ProductosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @Autowired private ProductosService servicio;
    @Autowired private InventarioService inv;

    @GetMapping("/menuprincipal")
    public String MostrarMenuPrincipal(Model modelo) {
        var lista = servicio.buscar("");
        modelo.addAttribute("totalProductos", lista.size());
        modelo.addAttribute("stockBajo", lista.stream().filter(p -> p.getStock() <= p.getStockMinimo()).count());
        modelo.addAttribute("unidades", lista.stream().mapToInt(p -> p.getStock()).sum());
        var venc = inv.proximosAVencer(30);
        modelo.addAttribute("vencidos", venc.stream().filter(l -> l.getDiasParaVencer() < 0).count());
        modelo.addAttribute("porVencer", venc.stream().filter(l -> l.getDiasParaVencer() >= 0).count());
        return "menuprincipal";
    }
}

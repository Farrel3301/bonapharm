package pe.com.bonapharm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.com.bonapharm.service.InventarioService;

@Controller
public class VencimientosController {

    @Autowired private InventarioService inv;

    // HU-09 consultar proximos a vencer + HU-10 alerta de vencimiento
    @GetMapping("/vencimientos")
    public String MostrarVencimientos(@RequestParam(defaultValue = "60") int dias, Model m) {
        var lista = inv.proximosAVencer(dias);
        m.addAttribute("lista", lista);
        m.addAttribute("dias", dias);
        m.addAttribute("vencidos", lista.stream().filter(l -> l.getDiasParaVencer() < 0).count());
        return "inventario/vencimientos";
    }
}

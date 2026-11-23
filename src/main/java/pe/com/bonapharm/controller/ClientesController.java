package pe.com.bonapharm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.bonapharm.service.ClientesService;

// HU-14 Registrar cliente (solo ADMIN: el interceptor bloquea al vendedor)
@Controller
@RequestMapping("/clientes")
public class ClientesController {

    @Autowired private ClientesService servicio;

    private String vista(Model m, String error) {
        m.addAttribute("lista", servicio.listar());
        if (error != null) m.addAttribute("error", error);
        return "clientes/clientes";
    }

    @GetMapping("/listar")
    public String MostrarClientes(Model m) { return vista(m, null); }

    @PostMapping("/registrar")
    public String RegistrarClientes(@RequestParam(required = false) String codigo, @RequestParam(required = false) String ruc, @RequestParam(required = false) String razonSocial,
            @RequestParam(required = false) String direccionOficina, @RequestParam(required = false) String direccionAlmacen,
            Model m, RedirectAttributes ra) {
        if (vacio(codigo) || vacio(ruc) || vacio(razonSocial) || vacio(direccionOficina) || vacio(direccionAlmacen))
            return vista(m, "Faltan datos requeridos: código, RUC, razón social, dirección de oficina y dirección del almacén.");
        if (!ruc.trim().matches("\\d{11}")) return vista(m, "El RUC debe tener 11 dígitos numéricos.");
        if (servicio.existeRuc(ruc.trim())) return vista(m, "Ya existe un cliente con ese RUC.");
        if (servicio.existeCodigo(codigo.trim())) return vista(m, "Ya existe un cliente con ese código.");
        servicio.registrar(codigo.trim(), ruc.trim(), razonSocial.trim(), direccionOficina.trim(), direccionAlmacen.trim());
        ra.addFlashAttribute("ok", "Cliente registrado correctamente.");
        return "redirect:/clientes/listar";
    }

    private boolean vacio(String s) { return s == null || s.isBlank(); }
}

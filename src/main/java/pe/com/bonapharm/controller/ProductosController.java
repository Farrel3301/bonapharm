package pe.com.bonapharm.controller;

import pe.com.bonapharm.entity.ProductosEntity;
import pe.com.bonapharm.service.ProductosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductosController {

    @Autowired private ProductosService servicio;

    // HU-02 Consultar productos
    @GetMapping("/listar")
    public String MostrarListarProductos(@RequestParam(defaultValue = "") String q, Model modelo) {
        modelo.addAttribute("lista", servicio.buscar(q));
        modelo.addAttribute("q", q);
        return "productos/listarproductos";
    }

    // HU-04 Consultar stock
    @GetMapping("/stock")
    public String MostrarStock(@RequestParam(defaultValue = "") String q, Model modelo) {
        var lista = servicio.buscar(q);
        modelo.addAttribute("lista", lista);
        modelo.addAttribute("q", q);
        modelo.addAttribute("noRegistrado", !q.isBlank() && lista.isEmpty());
        return "productos/stockproductos";
    }

    // HU-01 Registrar producto
    @GetMapping("/registro")
    public String MostrarRegistrarProductos(Model modelo) {
        modelo.addAttribute("producto", ProductosEntity.builder().stock(0).build());
        return "productos/formproductos";
    }

    @PostMapping("/registrar")
    public String RegistrarProductos(@ModelAttribute("producto") ProductosEntity obj, Model modelo, RedirectAttributes ra) {
        String error = validar(obj, true);
        if (error != null) {
            modelo.addAttribute("error", error);
            return "productos/formproductos";
        }    
        obj.setNumeroGuiaRemision(obj.getNumeroGuiaRemision().trim());
        obj.setNumeroLote(obj.getNumeroLote().trim());
        servicio.add(obj);
        ra.addFlashAttribute("ok", "Producto registrado correctamente.");
        return "redirect:/productos/listar";
    }

    // HU-03 Modificar producto
    @GetMapping("/actualiza/{id}")
    public String MostrarActualizarProductos(@PathVariable Long id, Model modelo, RedirectAttributes ra) {
        ProductosEntity p = servicio.findById(id);
        if (p == null) {
            ra.addFlashAttribute("error", "El producto no fue encontrado.");
            return "redirect:/productos/listar";
        }
        modelo.addAttribute("producto", p);
        return "productos/formproductos";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarProductos(@ModelAttribute("producto") ProductosEntity obj, @PathVariable Long id, Model modelo, RedirectAttributes ra) {
        String error = validar(obj, false);
        if (error != null) {
            obj.setId(id);
            modelo.addAttribute("error", error);
            return "productos/formproductos";
        }
        obj.setNumeroGuiaRemision(obj.getNumeroGuiaRemision().trim());
        obj.setNumeroLote(obj.getNumeroLote().trim());
        if (servicio.update(obj, id) == null) ra.addFlashAttribute("error", "El producto no fue encontrado.");
        else ra.addFlashAttribute("ok", "Producto actualizado correctamente.");
        return "redirect:/productos/listar";
    }

    @GetMapping("/deshabilitar/{id}")
    public String DeshabilitarProductos(@PathVariable Long id, RedirectAttributes ra) {
        servicio.delete(id);
        ra.addFlashAttribute("ok", "Producto deshabilitado.");
        return "redirect:/productos/listar";
    }

    private String validar(ProductosEntity p, boolean nuevo) {
        if  (vacio(p.getNombre()) || vacio(p.getCategoria()) || vacio(p.getNumeroGuiaRemision()) || vacio(p.getNumeroLote()) || p.getPrecio() == null
                || (nuevo && p.getStock() == null))
            return "Faltan datos requeridos: SKU, nombre, categoría, guía de remisión, lote, precio, stock y stock mínimo son obligatorios.";
        if (p.getPrecio().signum() < 0 || (nuevo && p.getStock() < 0))
            return "El precio y las cantidades no pueden ser negativos.";
        return null;
    }

    private boolean vacio(String s) { return s == null || s.isBlank(); }
}

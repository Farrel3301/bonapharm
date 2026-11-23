package pe.com.bonapharm.controller;

import pe.com.bonapharm.entity.ProductosEntity;
import pe.com.bonapharm.entity.CatalogoCategoriasEntity;
import pe.com.bonapharm.entity.CatalogoLotesEntity;
import pe.com.bonapharm.entity.CatalogoMedicamentosEntity;
import pe.com.bonapharm.service.CatalogoService;
import pe.com.bonapharm.service.ProductosService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductosController {

    @Autowired private ProductosService servicio;
    @Autowired private CatalogoService catalogo;

    // Listas desplegables (datos desde la BD) disponibles en todas las vistas de este controlador
    @ModelAttribute("medicamentos")
    public List<CatalogoMedicamentosEntity> medicamentos() { return catalogo.medicamentos(); }

    @ModelAttribute("categorias")
    public List<CatalogoCategoriasEntity> categorias() { return catalogo.categorias(); }

    @ModelAttribute("lotes")
    public List<CatalogoLotesEntity> lotes() { return catalogo.lotes(); }

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
        modelo.addAttribute("producto", ProductosEntity.builder().stock(0).stockMinimo(5).build());
        return "productos/formproductos";
    }

    @PostMapping("/registrar")
    public String RegistrarProductos(@ModelAttribute("producto") ProductosEntity obj, Model modelo, RedirectAttributes ra) {
        String error = validar(obj, true);
        if (error == null && servicio.existeCodigo(obj.getCodigo().trim(), null)) error = "Ya existe un producto con ese Código.";
        if (error != null) {
            modelo.addAttribute("error", error);
            return "productos/formproductos";
        }
        obj.setCodigo(obj.getCodigo().trim());
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
        if (error == null && servicio.existeCodigo(obj.getCodigo().trim(), id)) error = "Ya existe otro producto con ese Código.";
        if (error != null) {
            obj.setId(id);
            modelo.addAttribute("error", error);
            return "productos/formproductos";
        }
        obj.setCodigo(obj.getCodigo().trim());
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
        if (vacio(p.getCodigo()) || vacio(p.getNombre()) || vacio(p.getCategoria()) || p.getPrecio() == null
                || p.getStockMinimo() == null || (nuevo && p.getStock() == null))
            return "Faltan datos requeridos: Código, nombre, categoría, precio y stock mínimo son obligatorios.";
        if (p.getPrecio().signum() < 0 || p.getStockMinimo() < 0 || (nuevo && p.getStock() < 0))
            return "El precio y las cantidades no pueden ser negativos.";
        return null;
    }

    private boolean vacio(String s) { return s == null || s.isBlank(); }
}

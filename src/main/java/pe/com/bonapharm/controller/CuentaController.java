package pe.com.bonapharm.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.bonapharm.service.UsuariosService;

// Cambiar mi propia contrasena (admin y vendedor)
@Controller
@RequestMapping("/cuenta")
public class CuentaController {

    @Autowired private UsuariosService servicio;

    @GetMapping("/clave")
    public String MostrarCambiarClave() { return "cuenta/clave"; }

    @PostMapping("/clave")
    public String CambiarClave(@RequestParam String actual, @RequestParam String nueva, @RequestParam String confirmar,
            HttpSession s, Model m, RedirectAttributes ra) {
        String error = null;
        if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) error = "Faltan datos requeridos.";
        else if (nueva.length() < 4) error = "La nueva contraseña debe tener al menos 4 caracteres.";
        else if (!nueva.equals(confirmar)) error = "La confirmación no coincide con la nueva contraseña.";
        else if (nueva.equals(actual)) error = "La nueva contraseña debe ser distinta de la actual.";
        if (error == null) {
            try { servicio.cambiarClave((String) s.getAttribute("usuario"), actual, nueva); }
            catch (IllegalArgumentException e) { error = e.getMessage(); }
        }
        if (error != null) { m.addAttribute("error", error); return "cuenta/clave"; }
        ra.addFlashAttribute("ok", "Contraseña actualizada correctamente.");
        return "redirect:/cuenta/clave";
    }
}

package pe.com.bonapharm.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.bonapharm.service.UsuariosService;

@Controller
@RequestMapping("/usuarios")
public class UsuariosController {

    @Autowired private UsuariosService servicio;

    private String vista(Model m, String error) {
        m.addAttribute("lista", servicio.listar());
        if (error != null) m.addAttribute("error", error);
        return "usuarios/usuarios";
    }

    @GetMapping("/listar")
    public String MostrarUsuarios(Model m) { return vista(m, null); }

    @PostMapping("/estado/{id}")
    public String CambiarEstadoUsuarios(@PathVariable Long id, HttpSession s, RedirectAttributes ra) {
        try {
            boolean activo = servicio.cambiarEstado(id, (String) s.getAttribute("usuario"));
            ra.addFlashAttribute("ok", activo ? "Usuario habilitado." : "Usuario deshabilitado.");
        } catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/usuarios/listar";
    }

    @PostMapping("/clave/{id}")
    public String RestablecerClaveUsuarios(@PathVariable Long id, @RequestParam(required = false) String clave, RedirectAttributes ra) {
        if (clave == null || clave.length() < 4) ra.addFlashAttribute("error", "La contraseña debe tener al menos 4 caracteres.");
        else {
            try { servicio.restablecerClave(id, clave); ra.addFlashAttribute("ok", "Contraseña restablecida correctamente."); }
            catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        }
        return "redirect:/usuarios/listar";
    }

    @PostMapping("/registrar")
    public String RegistrarUsuarios(@RequestParam String nombre, @RequestParam String usuario, @RequestParam String clave,
            @RequestParam String rol, Model m, RedirectAttributes ra) {
        if (nombre.isBlank() || usuario.isBlank() || clave.isBlank()) return vista(m, "Faltan datos requeridos.");
        if (!rol.equals("ADMIN") && !rol.equals("VENDEDOR")) return vista(m, "Rol inválido.");
        if (clave.length() < 4) return vista(m, "La contraseña debe tener al menos 4 caracteres.");
        if (servicio.existeUsuario(usuario.trim())) return vista(m, "Ya existe un usuario con ese nombre de usuario.");
        servicio.registrar(usuario.trim(), nombre.trim(), clave, rol);
        ra.addFlashAttribute("ok", "Usuario registrado correctamente.");
        return "redirect:/usuarios/listar";
    }
}

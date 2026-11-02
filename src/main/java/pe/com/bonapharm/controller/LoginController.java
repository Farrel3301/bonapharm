package pe.com.bonapharm.controller;

import pe.com.bonapharm.entity.UsuariosEntity;
import pe.com.bonapharm.service.UsuariosService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired private UsuariosService servicio;

    @GetMapping("/")
    public String raiz() { return "redirect:/menuprincipal"; }

    @GetMapping("/login")
    public String MostrarLogin() { return "login"; }

    @PostMapping("/login")
    public String Ingresar(@RequestParam String usuario, @RequestParam String clave, HttpSession session, Model modelo) {
        UsuariosEntity u = servicio.autenticar(usuario.trim(), clave);
        if (u == null) {
            modelo.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }
        session.setAttribute("usuario", u.getUsuario());
        session.setAttribute("nombre", u.getNombre());
        session.setAttribute("rol", u.getRol());
        return "redirect:/menuprincipal";
    }

    @GetMapping("/logout")
    public String Salir(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

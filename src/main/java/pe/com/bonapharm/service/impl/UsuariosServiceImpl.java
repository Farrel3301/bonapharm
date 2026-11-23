package pe.com.bonapharm.service.impl;

import pe.com.bonapharm.entity.UsuariosEntity;
import pe.com.bonapharm.repository.UsuariosRepository;
import pe.com.bonapharm.service.UsuariosService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuariosServiceImpl implements UsuariosService {

    @Autowired private UsuariosRepository repositorio;
    @Autowired private BCryptPasswordEncoder encoder;

    @Override
    public UsuariosEntity autenticar(String usuario, String clave) {
        return repositorio.findByUsuario(usuario)
                .filter(u -> Boolean.TRUE.equals(u.getEstado()) && encoder.matches(clave, u.getContrasenaHash()))
                .orElse(null);
    }

    @Override
    public List<UsuariosEntity> listar() { return repositorio.findAll(); }

    @Override
    public boolean existeUsuario(String usuario) { return repositorio.existsByUsuario(usuario); }

    @Override
    public boolean cambiarEstado(Long id, String usuarioActual) {
        UsuariosEntity u = repositorio.findById(id).orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (u.getUsuario().equals(usuarioActual)) throw new IllegalArgumentException("No puedes deshabilitar tu propio usuario.");
        u.setEstado(!Boolean.TRUE.equals(u.getEstado()));
        repositorio.save(u);
        return u.getEstado();
    }

    @Override
    public void cambiarClave(String usuario, String actual, String nueva) {
        UsuariosEntity u = repositorio.findByUsuario(usuario).orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (!encoder.matches(actual, u.getContrasenaHash())) throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        if (encoder.matches(nueva, u.getContrasenaHash())) throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual.");
        u.setContrasenaHash(encoder.encode(nueva));
        repositorio.save(u);
    }

    @Override
    public void restablecerClave(Long id, String nueva) {
        UsuariosEntity u = repositorio.findById(id).orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        u.setContrasenaHash(encoder.encode(nueva));
        repositorio.save(u);
    }

    @Override
    public void registrar(String usuario, String nombre, String clave, String rol) {
        repositorio.save(UsuariosEntity.builder().usuario(usuario).nombre(nombre)
                .contrasenaHash(encoder.encode(clave)).rol(rol).estado(true).build());
    }
}

package pe.com.bonapharm.service.impl;

import pe.com.bonapharm.entity.UsuariosEntity;
import pe.com.bonapharm.repository.UsuariosRepository;
import pe.com.bonapharm.service.UsuariosService;
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
}

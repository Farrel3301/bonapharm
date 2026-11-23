package pe.com.bonapharm.service;

import java.util.List;
import pe.com.bonapharm.entity.UsuariosEntity;

public interface UsuariosService {
    UsuariosEntity autenticar(String usuario, String clave);
    List<UsuariosEntity> listar();
    boolean existeUsuario(String usuario);
    boolean cambiarEstado(Long id, String usuarioActual);
    void cambiarClave(String usuario, String claveActual, String claveNueva);
    void restablecerClave(Long id, String claveNueva);
    void registrar(String usuario, String nombre, String clave, String rol);
}

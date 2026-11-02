package pe.com.bonapharm.service;

import pe.com.bonapharm.entity.UsuariosEntity;

public interface UsuariosService {
    UsuariosEntity autenticar(String usuario, String clave);
}

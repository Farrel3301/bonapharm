package pe.com.bonapharm.service;

import java.util.List;
import pe.com.bonapharm.entity.ClientesEntity;

public interface ClientesService {
    List<ClientesEntity> listar();
    List<ClientesEntity> activos();
    boolean existeCodigo(String codigo);
    boolean existeRuc(String ruc);
    void registrar(String codigo, String ruc, String razonSocial, String direccionOficina, String direccionAlmacen);
}

package pe.com.bonapharm.service;

import java.time.LocalDate;
import java.util.List;
import pe.com.bonapharm.entity.VentasEntity;

public interface VentasService {
    VentasEntity registrar(String usuario, LocalDate emision, LocalDate traslado, Long clienteId, List<Long> productoIds, List<Long> loteIds, List<Integer> cantidades);
    List<VentasEntity> comprasDeCliente(Long clienteId);
    List<VentasEntity> listar(String usuario, String rol);
    VentasEntity obtener(Long id, String usuario, String rol);
    void cancelar(Long id, String usuario, String rol);
}

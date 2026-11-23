package pe.com.bonapharm.service;

import java.time.LocalDate;
import java.util.List;
import pe.com.bonapharm.entity.LotesEntity;

public interface InventarioService {
    void registrarEntrada(Long idProducto, String codigoLote, LocalDate vencimiento, int cantidad, String guia, String usuario);
    List<LotesEntity> proximosAVencer(int dias);
    List<LotesEntity> lotesVendibles(Long idProducto);
    List<LotesEntity> lotesConStock(Long idProducto);
    void registrarSalida(Long idLote, int cantidad, String motivo, String usuario);
}

package pe.com.bonapharm.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.bonapharm.entity.*;
import pe.com.bonapharm.repository.*;
import pe.com.bonapharm.service.InventarioService;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired private ProductosRepository productos;
    @Autowired private LotesRepository lotes;
    @Autowired private MovimientosRepository movimientos;

    // HU-05 entrada + HU-07 lote + HU-08 vencimiento: todo en una sola transaccion
    @Override
    @Transactional
    public void registrarEntrada(Long idProducto, String codigoLote, LocalDate vencimiento, int cantidad, String guia, String usuario) {
        ProductosEntity p = productos.findById(idProducto).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        LotesEntity l = lotes.findByProducto_IdAndCodigoLote(idProducto, codigoLote).orElse(null);
        if (l == null) {
            l = new LotesEntity();
            l.setProducto(p); l.setCodigoLote(codigoLote); l.setFechaVencimiento(vencimiento); l.setCantidad(0);
        } else if (!l.getFechaVencimiento().equals(vencimiento)) {
            throw new IllegalArgumentException("El lote " + codigoLote + " ya existe con vencimiento " + l.getFechaVencimiento() + ".");
        }
        l.setCantidad(l.getCantidad() + cantidad);
        lotes.save(l);
        p.setStock(p.getStock() + cantidad);
        productos.save(p);
        MovimientosEntity m = new MovimientosEntity();
        m.setProducto(p); m.setLote(l); m.setTipo("ENTRADA"); m.setCantidad(cantidad);
        m.setGuiaRemision(guia); m.setUsuario(usuario); m.setFecha(LocalDateTime.now());
        movimientos.save(m);
    }

    // Lotes con stock, incluidos los vencidos (para poder darlos de baja)
    @Override
    public List<LotesEntity> lotesConStock(Long idProducto) {
        return lotes.findByProducto_IdAndCantidadGreaterThanOrderByFechaVencimientoAsc(idProducto, 0);
    }

    // HU-06: la salida descuenta del lote y del producto en una sola transaccion
    @Override
    @Transactional
    public void registrarSalida(Long idLote, int cantidad, String motivo, String usuario) {
        LotesEntity l = lotes.findById(idLote).orElseThrow(() -> new IllegalArgumentException("El lote no existe."));
        if (cantidad > l.getCantidad()) throw new IllegalStateException("Stock insuficiente en el lote " + l.getCodigoLote() + ". Disponible: " + l.getCantidad());
        l.setCantidad(l.getCantidad() - cantidad);
        lotes.save(l);
        ProductosEntity p = l.getProducto();
        p.setStock(p.getStock() - cantidad);
        productos.save(p);
        MovimientosEntity m = new MovimientosEntity();
        m.setProducto(p); m.setLote(l); m.setTipo("SALIDA"); m.setCantidad(cantidad);
        m.setMotivo(motivo); m.setUsuario(usuario); m.setFecha(LocalDateTime.now());
        movimientos.save(m);
    }

    // Lotes con stock y no vencidos de un producto (el que vence primero va primero)
    @Override
    public List<LotesEntity> lotesVendibles(Long idProducto) {
        return lotes.findByProducto_IdAndCantidadGreaterThanAndFechaVencimientoGreaterThanEqualOrderByFechaVencimientoAsc(idProducto, 0, LocalDate.now());
    }

    // HU-09: lotes con stock que vencen dentro de N dias (incluye los ya vencidos)
    @Override
    public List<LotesEntity> proximosAVencer(int dias) {
        return lotes.findByCantidadGreaterThanAndFechaVencimientoLessThanEqualOrderByFechaVencimientoAsc(0, LocalDate.now().plusDays(dias));
    }
}

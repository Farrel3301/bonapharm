package pe.com.bonapharm.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.bonapharm.entity.*;
import pe.com.bonapharm.repository.*;
import pe.com.bonapharm.service.VentasService;

@Service
public class VentasServiceImpl implements VentasService {

    @Autowired private ProductosRepository productos;
    @Autowired private LotesRepository lotes;
    @Autowired private VentasRepository ventas;
    @Autowired private ClientesRepository clientes;
    @Autowired private MovimientosRepository movimientos;

    // Venta + validacion de stock + descuento automatico (todo o nada)
    @Override
    @Transactional
    public VentasEntity registrar(String usuario, LocalDate emision, LocalDate traslado, Long clienteId, List<Long> ids, List<Long> loteIds, List<Integer> cants) {
        if (emision == null || traslado == null) throw new IllegalArgumentException("Indica la fecha de emisión y la fecha de traslado.");
        if (traslado.isBefore(emision)) throw new IllegalArgumentException("La fecha de traslado no puede ser anterior a la de emisión.");
        if (ids == null || ids.isEmpty()) throw new IllegalArgumentException("Agrega al menos un producto.");
        if (clienteId == null) throw new IllegalArgumentException("Selecciona el cliente destinatario.");
        ClientesEntity c = clientes.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
        VentasEntity v = new VentasEntity();
        v.setUsuario(usuario); v.setFecha(LocalDateTime.now());
        v.setCliente(c); v.setDestinatario(c.getRazonSocial()); v.setRucDestinatario(c.getRuc());
        v.setDireccionDestinatario(c.getDireccionOficina()); v.setPuntoLlegada(c.getDireccionAlmacen());
        v.setFechaEmision(emision); v.setFechaTraslado(traslado); v.setEstado("ACTIVA");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < ids.size(); i++) {
            ProductosEntity p = productos.findById(ids.get(i)).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
            int pedir = cants == null || i >= cants.size() || cants.get(i) == null ? 0 : cants.get(i);
            if (pedir <= 0) throw new IllegalArgumentException("Cantidad inválida para " + p.getNombre() + ".");
            Long lid = loteIds == null || i >= loteIds.size() ? null : loteIds.get(i);
            List<LotesEntity> ls;
            String donde = "";
            if (lid != null && lid != 0) {   // lote elegido por el usuario
                LotesEntity l = lotes.findById(lid).orElseThrow(() -> new IllegalArgumentException("Lote no encontrado."));
                if (!l.getProducto().getId().equals(p.getId())) throw new IllegalArgumentException("El lote no corresponde al producto.");
                if (l.getFechaVencimiento().isBefore(LocalDate.now())) throw new IllegalStateException("El lote " + l.getCodigoLote() + " está vencido.");
                ls = List.of(l);
                donde = " en el lote " + l.getCodigoLote();
            } else {                          // automatico: el que vence primero (FEFO)
                ls = lotes.findByProducto_IdAndCantidadGreaterThanAndFechaVencimientoGreaterThanEqualOrderByFechaVencimientoAsc(p.getId(), 0, LocalDate.now());
            }
            int disponible = ls.stream().mapToInt(LotesEntity::getCantidad).sum();
            if (pedir > disponible) throw new IllegalStateException("Stock insuficiente de " + p.getNombre() + donde + ". Disponible: " + disponible);
            int resto = pedir;
            for (LotesEntity l : ls) {
                if (resto == 0) break;
                int t = Math.min(resto, l.getCantidad());
                l.setCantidad(l.getCantidad() - t);
                lotes.save(l);
                resto -= t;
                DetalleVentasEntity d = new DetalleVentasEntity();
                d.setVenta(v); d.setProducto(p); d.setLote(l); d.setCantidad(t);
                d.setPrecioUnitario(p.getPrecio());
                d.setSubtotal(p.getPrecio().multiply(BigDecimal.valueOf(t)));
                v.getDetalles().add(d);
                mov(p, l, "VENTA", t, "Venta", usuario);
                subtotal = subtotal.add(d.getSubtotal());
            }
            p.setStock(p.getStock() - pedir);
            productos.save(p);
        }
        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        v.setSubtotal(subtotal); v.setIgv(igv); v.setTotal(subtotal.add(igv));
        return ventas.save(v);
    }

    // Toda variacion de stock deja un movimiento (historial unico)
    private void mov(ProductosEntity p, LotesEntity l, String tipo, int cant, String motivo, String usuario) {
        MovimientosEntity m = new MovimientosEntity();
        m.setProducto(p); m.setLote(l); m.setTipo(tipo); m.setCantidad(cant);
        m.setMotivo(motivo); m.setUsuario(usuario); m.setFecha(LocalDateTime.now());
        movimientos.save(m);
    }

    @Override
    public List<VentasEntity> comprasDeCliente(Long clienteId) { return ventas.findByCliente_IdOrderByFechaDesc(clienteId); }

    @Override
    public List<VentasEntity> listar(String usuario, String rol) {
        return "VENDEDOR".equals(rol) ? ventas.findByUsuarioOrderByFechaDesc(usuario) : ventas.findAllByOrderByFechaDesc();
    }

    @Override
    public VentasEntity obtener(Long id, String usuario, String rol) {
        VentasEntity v = ventas.findById(id).orElseThrow(() -> new IllegalArgumentException("La venta no existe."));
        return v;
    }

    // Cancelar: la mercaderia regresa a su lote y al stock del producto
    @Override
    @Transactional
    public void cancelar(Long id, String usuario, String rol) {
        VentasEntity v = obtener(id, usuario, rol);
        if ("VENDEDOR".equals(rol) && !v.getUsuario().equals(usuario)) throw new IllegalArgumentException("No tienes permiso sobre esta venta.");
        if ("CANCELADA".equals(v.getEstado())) throw new IllegalStateException("La venta ya está cancelada.");
        for (DetalleVentasEntity d : v.getDetalles()) {
            LotesEntity l = d.getLote();
            l.setCantidad(l.getCantidad() + d.getCantidad());
            lotes.save(l);
            ProductosEntity p = d.getProducto();
            p.setStock(p.getStock() + d.getCantidad());
            productos.save(p);
            mov(p, l, "ANULACION", d.getCantidad(), "Venta cancelada", usuario);
        }
        v.setEstado("CANCELADA");
        ventas.save(v);
    }
}

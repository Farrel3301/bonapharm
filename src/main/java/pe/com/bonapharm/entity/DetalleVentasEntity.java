package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Getter @Setter
@Entity(name = "DetalleVentasEntity")
@Table(name = "detalle_ventas")
public class DetalleVentasEntity {
    @Id @Column(name = "id_detalle") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "id_venta", nullable = false)
    private VentasEntity venta;
    @ManyToOne @JoinColumn(name = "id_producto", nullable = false)
    private ProductosEntity producto;
    @ManyToOne @JoinColumn(name = "id_lote", nullable = false)
    private LotesEntity lote;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;
}

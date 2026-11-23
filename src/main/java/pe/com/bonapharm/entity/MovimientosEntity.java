package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter
@Entity(name = "MovimientosEntity")
@Table(name = "movimientos_inventario")
public class MovimientosEntity {
    @Id @Column(name = "id_movimiento") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "id_producto", nullable = false)
    private ProductosEntity producto;
    @ManyToOne @JoinColumn(name = "id_lote")
    private LotesEntity lote;
    @Column(length = 10, nullable = false)
    private String tipo;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(length = 60)
    private String motivo;
    @Column(name = "guia_remision", length = 30)
    private String guiaRemision;
    @Column(length = 50, nullable = false)
    private String usuario;
    @Column(nullable = false)
    private LocalDateTime fecha;
}

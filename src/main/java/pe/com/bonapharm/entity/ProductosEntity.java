package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity(name = "ProductosEntity")
@Table(name = "productos")
public class ProductosEntity {
    @Id
    @Column(name = "id_producto")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String sku;

    @Column(length = 120, nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 60, nullable = false)
    private String categoria;

    @Column(name = "numero_guia_remision", length = 30, nullable = false)
    private String numeroGuiaRemision;

    @Column(name = "numero_lote", length = 40, nullable = false)
    private String numeroLote;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private Boolean estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;
}

package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.*;

@Getter @Setter
@Entity(name = "LotesEntity")
@Table(name = "lotes")
public class LotesEntity {
    @Id @Column(name = "id_lote") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "id_producto", nullable = false)
    private ProductosEntity producto;
    @Column(name = "codigo_lote", length = 40, nullable = false)
    private String codigoLote;
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    @Column(nullable = false)
    private Integer cantidad;

    public long getDiasParaVencer() { return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento); }
}

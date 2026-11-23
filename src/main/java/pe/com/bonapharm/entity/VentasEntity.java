package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Getter @Setter
@Entity(name = "VentasEntity")
@Table(name = "ventas")
public class VentasEntity {
    @Id @Column(name = "id_venta") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String usuario;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;
    @Column(name = "fecha_traslado", nullable = false)
    private LocalDate fechaTraslado;
    @ManyToOne @JoinColumn(name = "id_cliente", nullable = false)
    private ClientesEntity cliente;
    // Copia de los datos del cliente al momento de la venta (la guia no cambia si el cliente se edita despues)
    @Column(length = 150, nullable = false)
    private String destinatario;
    @Column(name = "ruc_destinatario", length = 11, nullable = false)
    private String rucDestinatario;
    @Column(name = "direccion_destinatario", length = 250, nullable = false)
    private String direccionDestinatario;
    @Column(name = "punto_llegada", length = 250, nullable = false)
    private String puntoLlegada;
    @Column(length = 12, nullable = false)
    private String estado;                       // ACTIVA o CANCELADA
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal igv;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVentasEntity> detalles = new ArrayList<>();

    // Correlativo de la guia de remision: serie T001 + numero de venta
    public String getNumeroGuia() { return "T001-" + String.format("%08d", id); }
}

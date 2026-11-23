package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Data @Builder
@Entity(name = "ClientesEntity")
@Table(name = "clientes")
public class ClientesEntity {
    @Id @Column(name = "id_cliente") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "codigo_cliente", length = 30, nullable = false, unique = true)
    private String codigo;
    @Column(length = 11, nullable = false, unique = true)
    private String ruc;
    @Column(name = "razon_social", length = 150, nullable = false)
    private String razonSocial;
    @Column(name = "direccion_oficina", length = 250, nullable = false)
    private String direccionOficina;
    @Column(name = "direccion_almacen", length = 250, nullable = false)
    private String direccionAlmacen;
    @Column(nullable = false)
    private Boolean estado;
}

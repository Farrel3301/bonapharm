package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Data
@Entity(name = "CatalogoMotivosEntity")
@Table(name = "catalogo_motivos_salida")
public class CatalogoMotivosEntity {
    @Id @Column(name = "id_motivo") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 60, nullable = false, unique = true)
    private String nombre;
}

package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name = "CatalogoMedicamentosEntity")
@Table(name = "catalogo_medicamentos")
public class CatalogoMedicamentosEntity {
    @Id
    @Column(name = "id_medicamento")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_producto", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(length = 120, nullable = false)
    private String nombre;
}

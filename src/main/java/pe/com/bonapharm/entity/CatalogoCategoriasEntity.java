package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name = "CatalogoCategoriasEntity")
@Table(name = "catalogo_categorias")
public class CatalogoCategoriasEntity {
    @Id
    @Column(name = "id_categoria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60, nullable = false, unique = true)
    private String nombre;
}

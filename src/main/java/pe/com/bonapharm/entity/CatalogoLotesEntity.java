package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name = "CatalogoLotesEntity")
@Table(name = "catalogo_lotes")
public class CatalogoLotesEntity {
    @Id
    @Column(name = "id_catalogo_lote")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_lote", length = 40, nullable = false, unique = true)
    private String codigoLote;
}

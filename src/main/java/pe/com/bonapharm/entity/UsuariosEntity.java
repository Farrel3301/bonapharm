package pe.com.bonapharm.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity(name = "UsuariosEntity")
@Table(name = "usuarios")
public class UsuariosEntity {
    @Id
    @Column(name = "id_usuario")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String usuario;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(name = "contrasena_hash", length = 255, nullable = false)
    private String contrasenaHash;

    @Column(length = 30, nullable = false)
    private String rol;

    @Column(nullable = false)
    private Boolean estado;
}

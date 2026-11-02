package pe.com.bonapharm.repository;

import pe.com.bonapharm.entity.UsuariosEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosRepository extends JpaRepository<UsuariosEntity, Long> {
    Optional<UsuariosEntity> findByUsuario(String usuario);
}

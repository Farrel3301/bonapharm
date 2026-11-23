package pe.com.bonapharm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.bonapharm.entity.VentasEntity;

public interface VentasRepository extends JpaRepository<VentasEntity, Long> {
    List<VentasEntity> findAllByOrderByFechaDesc();
    List<VentasEntity> findByCliente_IdOrderByFechaDesc(Long clienteId);
    List<VentasEntity> findByUsuarioOrderByFechaDesc(String usuario);
}

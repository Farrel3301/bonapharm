package pe.com.bonapharm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.bonapharm.entity.ClientesEntity;

public interface ClientesRepository extends JpaRepository<ClientesEntity, Long> {
    boolean existsByCodigo(String codigo);
    boolean existsByRuc(String ruc);
    List<ClientesEntity> findAllByOrderByRazonSocialAsc();
    List<ClientesEntity> findByEstadoTrueOrderByRazonSocialAsc();
}

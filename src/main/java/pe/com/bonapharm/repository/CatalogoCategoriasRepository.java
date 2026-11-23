package pe.com.bonapharm.repository;

import pe.com.bonapharm.entity.CatalogoCategoriasEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoCategoriasRepository extends JpaRepository<CatalogoCategoriasEntity, Long> {
    List<CatalogoCategoriasEntity> findAllByOrderByNombreAsc();
}

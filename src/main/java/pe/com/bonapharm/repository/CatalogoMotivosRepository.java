package pe.com.bonapharm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.bonapharm.entity.CatalogoMotivosEntity;

public interface CatalogoMotivosRepository extends JpaRepository<CatalogoMotivosEntity, Long> {
    List<CatalogoMotivosEntity> findAllByOrderByNombreAsc();
}

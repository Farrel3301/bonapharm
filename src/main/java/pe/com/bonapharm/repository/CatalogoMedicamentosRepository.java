package pe.com.bonapharm.repository;

import pe.com.bonapharm.entity.CatalogoMedicamentosEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoMedicamentosRepository extends JpaRepository<CatalogoMedicamentosEntity, Long> {
    List<CatalogoMedicamentosEntity> findAllByOrderByNombreAsc();
}

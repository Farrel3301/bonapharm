package pe.com.bonapharm.repository;

import pe.com.bonapharm.entity.CatalogoLotesEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoLotesRepository extends JpaRepository<CatalogoLotesEntity, Long> {
    List<CatalogoLotesEntity> findAllByOrderByCodigoLoteAsc();
}

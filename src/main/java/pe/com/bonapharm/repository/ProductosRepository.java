package pe.com.bonapharm.repository;

import pe.com.bonapharm.entity.ProductosEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductosRepository extends JpaRepository<ProductosEntity, Long> {

    @Query("SELECT p FROM ProductosEntity p WHERE p.estado = true AND "
         + "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :q, '%'))) "
         + "ORDER BY p.nombre")
    List<ProductosEntity> buscar(@Param("q") String q);

    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}

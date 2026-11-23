package pe.com.bonapharm.repository;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.bonapharm.entity.LotesEntity;

public interface LotesRepository extends JpaRepository<LotesEntity, Long> {
    Optional<LotesEntity> findByProducto_IdAndCodigoLote(Long idProducto, String codigoLote);
    List<LotesEntity> findByProducto_IdAndCantidadGreaterThanOrderByFechaVencimientoAsc(Long idProducto, Integer cantidad);
    // Lotes vendibles: con stock y no vencidos, el que vence primero va primero (FEFO)
    List<LotesEntity> findByProducto_IdAndCantidadGreaterThanAndFechaVencimientoGreaterThanEqualOrderByFechaVencimientoAsc(Long idProducto, Integer cantidad, LocalDate desde);
    List<LotesEntity> findByCantidadGreaterThanAndFechaVencimientoLessThanEqualOrderByFechaVencimientoAsc(Integer cantidad, LocalDate hasta);
}

package pe.com.bonapharm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.bonapharm.entity.MovimientosEntity;

public interface MovimientosRepository extends JpaRepository<MovimientosEntity, Long> { }

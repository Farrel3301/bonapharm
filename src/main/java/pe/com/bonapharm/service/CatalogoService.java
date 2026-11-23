package pe.com.bonapharm.service;

import pe.com.bonapharm.entity.CatalogoCategoriasEntity;
import pe.com.bonapharm.entity.CatalogoLotesEntity;
import pe.com.bonapharm.entity.CatalogoMotivosEntity;
import pe.com.bonapharm.entity.CatalogoMedicamentosEntity;
import java.util.List;

public interface CatalogoService {
    List<CatalogoMedicamentosEntity> medicamentos();
    List<CatalogoLotesEntity> lotes();
    List<CatalogoCategoriasEntity> categorias();
    List<CatalogoMotivosEntity> motivos();
}

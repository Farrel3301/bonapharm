package pe.com.bonapharm.service.impl;

import pe.com.bonapharm.entity.CatalogoCategoriasEntity;
import pe.com.bonapharm.entity.CatalogoLotesEntity;
import pe.com.bonapharm.entity.CatalogoMotivosEntity;
import pe.com.bonapharm.entity.CatalogoMedicamentosEntity;
import pe.com.bonapharm.repository.CatalogoCategoriasRepository;
import pe.com.bonapharm.repository.CatalogoLotesRepository;
import pe.com.bonapharm.repository.CatalogoMotivosRepository;
import pe.com.bonapharm.repository.CatalogoMedicamentosRepository;
import pe.com.bonapharm.service.CatalogoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    @Autowired private CatalogoMedicamentosRepository medicamentosRepo;
    @Autowired private CatalogoLotesRepository lotesRepo;
    @Autowired private CatalogoCategoriasRepository categoriasRepo;
    @Autowired private CatalogoMotivosRepository motivosRepo;

    @Override
    public List<CatalogoMedicamentosEntity> medicamentos() { return medicamentosRepo.findAllByOrderByNombreAsc(); }

    @Override
    public List<CatalogoLotesEntity> lotes() { return lotesRepo.findAllByOrderByCodigoLoteAsc(); }

    @Override
    public List<CatalogoCategoriasEntity> categorias() { return categoriasRepo.findAllByOrderByNombreAsc(); }

    @Override
    public List<CatalogoMotivosEntity> motivos() { return motivosRepo.findAllByOrderByNombreAsc(); }
}

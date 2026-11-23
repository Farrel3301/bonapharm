package pe.com.bonapharm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.bonapharm.entity.ClientesEntity;
import pe.com.bonapharm.repository.ClientesRepository;
import pe.com.bonapharm.service.ClientesService;

@Service
public class ClientesServiceImpl implements ClientesService {

    @Autowired private ClientesRepository repositorio;

    @Override public List<ClientesEntity> listar() { return repositorio.findAllByOrderByRazonSocialAsc(); }
    @Override public List<ClientesEntity> activos() { return repositorio.findByEstadoTrueOrderByRazonSocialAsc(); }
    @Override public boolean existeCodigo(String codigo) { return repositorio.existsByCodigo(codigo); }
    @Override public boolean existeRuc(String ruc) { return repositorio.existsByRuc(ruc); }

    @Override
    public void registrar(String codigo, String ruc, String razonSocial, String direccionOficina, String direccionAlmacen) {
        repositorio.save(ClientesEntity.builder().codigo(codigo).ruc(ruc).razonSocial(razonSocial)
                .direccionOficina(direccionOficina).direccionAlmacen(direccionAlmacen).estado(true).build());
    }
}

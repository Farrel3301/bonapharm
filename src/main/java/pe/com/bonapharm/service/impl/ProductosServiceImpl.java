package pe.com.bonapharm.service.impl;

import pe.com.bonapharm.entity.ProductosEntity;
import pe.com.bonapharm.repository.ProductosRepository;
import pe.com.bonapharm.service.ProductosService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductosServiceImpl implements ProductosService {

    @Autowired private ProductosRepository repositorio;

    @Override
    public List<ProductosEntity> buscar(String q) { return repositorio.buscar(q == null ? "" : q.trim()); }

    @Override
    public ProductosEntity findById(Long id) { return repositorio.findById(id).orElse(null); }

    @Override
    public ProductosEntity add(ProductosEntity obj) {
        obj.setEstado(true);
        obj.setFechaCreacion(LocalDate.now());
        if (obj.getStock() == null) obj.setStock(0);
        return repositorio.save(obj);
    }

    // El stock no se edita aqui: lo mueven entradas, salidas y ventas
    @Override
    public ProductosEntity update(ProductosEntity obj, Long id) {
        ProductosEntity a = repositorio.findById(id).orElse(null);
        if (a == null) return null;
        a.setCodigo(obj.getCodigo());
        a.setNombre(obj.getNombre());
        a.setDescripcion(obj.getDescripcion());
        a.setCategoria(obj.getCategoria());
        a.setPrecio(obj.getPrecio());
        a.setStockMinimo(obj.getStockMinimo());
        return repositorio.save(a);
    }

    @Override
    public ProductosEntity delete(Long id) {
        ProductosEntity a = repositorio.findById(id).orElse(null);
        if (a == null) return null;
        a.setEstado(false);
        return repositorio.save(a);
    }

    @Override
    public boolean existeCodigo(String codigo, Long idExcluir) {
        return idExcluir == null ? repositorio.existsByCodigo(codigo) : repositorio.existsByCodigoAndIdNot(codigo, idExcluir);
    }
}

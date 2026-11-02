package pe.com.bonapharm.service;

import pe.com.bonapharm.entity.ProductosEntity;
import java.util.List;

public interface ProductosService {
    List<ProductosEntity> buscar(String q);
    ProductosEntity findById(Long id);
    ProductosEntity add(ProductosEntity obj);
    ProductosEntity update(ProductosEntity obj, Long id);
    ProductosEntity delete(Long id);
    boolean existeSku(String sku, Long idExcluir);
}

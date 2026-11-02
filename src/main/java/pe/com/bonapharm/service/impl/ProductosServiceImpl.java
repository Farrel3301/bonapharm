package pe.com.bonapharm.service.impl;

import pe.com.bonapharm.entity.ProductosEntity;
import pe.com.bonapharm.repository.ProductosRepository;
import pe.com.bonapharm.service.ProductosService;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductosServiceImpl implements ProductosService {
	
	private static final int DIAS_ENTREGA = 7;
	private static final int DIAS_SEGURIDAD = 2;
	private static final double CONSUMO_DEFECTO = 2.0;
	private static final  Map<String, Double> CONSUMO_CATEGORIA = Map.of(
			"analgesicos", 6.0,
			"antibioticos", 3.0,
			"antialergicos", 2.0,
			"vitaminas", 2.0,
			"cuidado personal", 1.5)
			;
	

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
        obj.setSku(generarSku(obj.getCategoria()));
        obj.setStockMinimo(calcularStockMinimo(obj.getCategoria()));
        return repositorio.save(obj);
    }

    // El stock no se edita aqui: lo mueven entradas, salidas y ventas
    @Override
    public ProductosEntity update(ProductosEntity obj, Long id) {
        ProductosEntity a = repositorio.findById(id).orElse(null);
        if (a == null) return null;
        a.setNombre(obj.getNombre());
        a.setDescripcion(obj.getDescripcion());
        a.setCategoria(obj.getCategoria());
        a.setNumeroGuiaRemision(obj.getNumeroGuiaRemision());
        a.setNumeroLote(obj.getNumeroLote());
        a.setPrecio(obj.getPrecio());
        a.setStockMinimo(calcularStockMinimo(a.getCategoria()));
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
    public boolean existeSku(String sku, Long idExcluir) {
        return idExcluir == null ? repositorio.existsBySku(sku) : repositorio.existsBySkuAndIdNot(sku, idExcluir);
    }
    private String generarSku(String categoria) {
    	 String prefijo = Normalizer.normalize(categoria == null ? "" : categoria, Normalizer.Form.NFD)
                 .replaceAll("[^A-Za-z]", "").toUpperCase();
         if (prefijo.isEmpty()) prefijo = "PRO";
         prefijo = prefijo.substring(0, Math.min(3, prefijo.length()));

         int n = (int) repositorio.countBySkuStartingWith(prefijo + "-") + 1;
         String sku = String.format("%s-%04d", prefijo, n);
         while (repositorio.existsBySku(sku)) {
             sku = String.format("%s-%04d", prefijo, ++n);
         }
         return sku;
    }
    private String normalizar(String s) {
    	return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD)
    			.replaceAll ("\\p{M}","").toLowerCase().trim();
    }
    
    private int calcularStockMinimo(String categoria) {
        double consumoDiario = CONSUMO_CATEGORIA.getOrDefault(normalizar(categoria), CONSUMO_DEFECTO);
        return (int) Math.ceil(consumoDiario * DIAS_ENTREGA + consumoDiario * DIAS_SEGURIDAD);
    }
}

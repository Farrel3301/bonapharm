package pe.com.bonapharm.service;

import java.util.List;
import lombok.Getter;

// Estructura generica de un reporte: sirve para la pantalla, el PDF y el Excel
@Getter
public class Reporte {
    private final String titulo;
    private final String filtros;
    private final List<String> columnas;
    private final List<List<String>> filas;
    private final List<String[]> totales;      // {etiqueta, valor}
    private final String mensajeVacio;
    // Segunda tabla opcional (detalle por lote del inventario)
    private final String tituloDetalle;
    private final List<String> columnasDetalle;
    private final List<List<String>> filasDetalle;

    public Reporte(String titulo, String filtros, List<String> columnas, List<List<String>> filas, List<String[]> totales, String mensajeVacio) {
        this(titulo, filtros, columnas, filas, totales, mensajeVacio, null, null, null);
    }

    public Reporte(String titulo, String filtros, List<String> columnas, List<List<String>> filas, List<String[]> totales, String mensajeVacio,
            String tituloDetalle, List<String> columnasDetalle, List<List<String>> filasDetalle) {
        this.titulo = titulo; this.filtros = filtros; this.columnas = columnas; this.filas = filas; this.totales = totales; this.mensajeVacio = mensajeVacio;
        this.tituloDetalle = tituloDetalle; this.columnasDetalle = columnasDetalle; this.filasDetalle = filasDetalle;
    }

    public boolean tieneDetalle() { return filasDetalle != null && !filasDetalle.isEmpty(); }
}

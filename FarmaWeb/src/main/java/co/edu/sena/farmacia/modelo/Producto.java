package co.edu.sena.farmacia.modelo;

import java.math.BigDecimal;
import co.edu.sena.farmacia.util.FormatoMoneda;

/**
 * Representa la tabla TblProducto: el catalogo de medicamentos y productos
 * que la farmacia ofrece a sus clientes.
 */
public class Producto {

    private int idProducto;
    private String nombre;
    private String skuCode;
    private String descripcion;
    private BigDecimal precio;
    private boolean requiereReceta;
    private String categoria;
    private String imagen;

    /** Suma del stock de todos los lotes, calculada en la consulta. */
    private double stockTotal;

    public Producto() {
    }

    public Producto(int idProducto, String nombre, BigDecimal precio) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
    }

    /**
     * Indica si el producto se puede vender segun su inventario.
     *
     * @return true cuando existe al menos una unidad disponible en los lotes
     */
    public boolean estaDisponible() {
        return stockTotal > 0;
    }

    /**
     * Traduce la disponibilidad a un texto para mostrar en el catalogo.
     *
     * @return "Disponible" o "Agotado"
     */
    public String describirDisponibilidad() {
        return estaDisponible() ? "Disponible" : "Agotado";
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public double getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(double stockTotal) {
        this.stockTotal = stockTotal;
    }

    public boolean getDisponible() {
        return estaDisponible();
    }

    public String getDisponibilidad() {
        return describirDisponibilidad();
    }

    /**
     * Indica si el producto tiene una imagen registrada para el catalogo.
     *
     * @return true cuando se puede mostrar la foto del producto
     */
    public boolean tieneImagen() {
        return imagen != null && !imagen.trim().isEmpty();
    }

    /**
     * Entrega la letra con que se dibuja el recuadro cuando no hay foto.
     *
     * @return primera letra del nombre en mayuscula
     */
    public String obtenerInicial() {
        if (nombre == null || nombre.isEmpty()) {
            return "?";
        }
        return nombre.substring(0, 1).toUpperCase();
    }

    public boolean getConImagen() {
        return tieneImagen();
    }

    public String getInicial() {
        return obtenerInicial();
    }
    /** Precio con separador de miles, para mostrarlo en las vistas. */
    public String getPrecioTexto() {
        return FormatoMoneda.enPesos(precio);
    }

    /** Stock sin decimales, para mostrarlo en las vistas. */
    public String getStockTexto() {
        return FormatoMoneda.enUnidades(stockTotal);
    }

}

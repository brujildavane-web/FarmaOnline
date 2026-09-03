package co.edu.sena.farmacia.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import co.edu.sena.farmacia.util.FormatoMoneda;

/**
 * Renglon del carrito de compras.
 *
 * No corresponde a ninguna tabla: el carrito vive en la sesion del visitante
 * hasta que confirma el pedido, momento en el que cada item se convierte en
 * una fila de TblLineaPedido.
 */
public class ItemCarrito implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idProducto;
    private String nombre;
    private String skuCode;
    private BigDecimal precio;
    private double cantidad;
    private boolean requiereReceta;

    /**
     * Datos de la formula medica que el cliente adjunta cuando el producto
     * es controlado. Se conservan en la sesion hasta que se confirma el
     * pedido, porque TblFormulaMedica exige un pedido que aun no existe.
     */
    private String archivoReceta;
    private LocalDate fechaPrescripcion;

    public ItemCarrito() {
    }

    public ItemCarrito(Producto producto, double cantidad) {
        this.idProducto = producto.getIdProducto();
        this.nombre = producto.getNombre();
        this.skuCode = producto.getSkuCode();
        this.precio = producto.getPrecio();
        this.requiereReceta = producto.isRequiereReceta();
        this.cantidad = cantidad;
    }

    /**
     * Multiplica el precio unitario por la cantidad solicitada.
     *
     * @return subtotal del renglon con dos decimales
     */
    public BigDecimal calcularSubtotal() {
        if (precio == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public boolean isRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    public BigDecimal getSubtotal() {
        return calcularSubtotal();
    }
    /** Precio unitario con separador de miles. */
    public String getPrecioTexto() {
        return FormatoMoneda.enPesos(precio);
    }

    /** Subtotal del renglon con separador de miles. */
    public String getSubtotalTexto() {
        return FormatoMoneda.enPesos(calcularSubtotal());
    }

    /** Cantidad sin decimales. */
    public String getCantidadTexto() {
        return FormatoMoneda.enUnidades(cantidad);
    }

    /**
     * Indica si el renglon ya tiene su formula medica adjunta.
     *
     * @return true cuando hay un archivo registrado
     */
    public boolean tieneReceta() {
        return archivoReceta != null && !archivoReceta.trim().isEmpty();
    }

    /**
     * Indica si al renglon le falta la formula medica para poder comprarse.
     *
     * @return true cuando el producto es controlado y no tiene archivo
     */
    public boolean faltaReceta() {
        return requiereReceta && !tieneReceta();
    }

    public String getArchivoReceta() {
        return archivoReceta;
    }

    public void setArchivoReceta(String archivoReceta) {
        this.archivoReceta = archivoReceta;
    }

    public LocalDate getFechaPrescripcion() {
        return fechaPrescripcion;
    }

    public void setFechaPrescripcion(LocalDate fechaPrescripcion) {
        this.fechaPrescripcion = fechaPrescripcion;
    }

    public boolean getConReceta() {
        return tieneReceta();
    }

    public boolean getSinReceta() {
        return faltaReceta();
    }

}

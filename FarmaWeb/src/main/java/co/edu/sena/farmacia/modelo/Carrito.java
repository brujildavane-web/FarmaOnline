package co.edu.sena.farmacia.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import co.edu.sena.farmacia.util.FormatoMoneda;

/**
 * Carrito de compras del visitante.
 *
 * Se guarda en la sesion HTTP y no en la base de datos, porque TblPedido
 * exige un cliente y un farmaceutico que todavia no se conocen mientras la
 * persona solo esta mirando el catalogo. Al confirmar la compra el carrito
 * se traduce a un Pedido con sus lineas.
 */
public class Carrito implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Impuesto al valor agregado vigente en Colombia. */
    public static final BigDecimal IVA_COLOMBIA = new BigDecimal("0.19");

    private final List<ItemCarrito> items = new ArrayList<>();

    /**
     * Agrega un producto o acumula la cantidad si ya estaba en el carrito.
     *
     * @param producto producto tomado del catalogo
     * @param cantidad unidades que se agregan
     */
    public void agregar(Producto producto, double cantidad) {
        ItemCarrito existente = buscarItem(producto.getIdProducto());

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
            return;
        }

        items.add(new ItemCarrito(producto, cantidad));
    }

    /**
     * Cambia la cantidad de un renglon ya presente en el carrito.
     *
     * @param idProducto identificador del producto
     * @param cantidad cantidad nueva; si es cero o menos, el renglon se quita
     */
    public void cambiarCantidad(int idProducto, double cantidad) {
        ItemCarrito item = buscarItem(idProducto);
        if (item == null) {
            return;
        }

        if (cantidad <= 0) {
            items.remove(item);
        } else {
            item.setCantidad(cantidad);
        }
    }

    /**
     * Quita un producto del carrito.
     *
     * @param idProducto identificador del producto
     */
    public void quitar(int idProducto) {
        ItemCarrito item = buscarItem(idProducto);
        if (item != null) {
            items.remove(item);
        }
    }

    /** Deja el carrito sin renglones, tras confirmar el pedido. */
    public void vaciar() {
        items.clear();
    }

    /**
     * Busca el renglon que corresponde a un producto.
     *
     * @param idProducto identificador del producto
     * @return el renglon encontrado, o null si el producto no esta en el carrito
     */
    public ItemCarrito buscarItem(int idProducto) {
        for (ItemCarrito item : items) {
            if (item.getIdProducto() == idProducto) {
                return item;
            }
        }
        return null;
    }

    /**
     * Suma los subtotales de todos los renglones.
     *
     * @return valor de los productos antes de impuestos
     */
    public BigDecimal calcularSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : items) {
            total = total.add(item.calcularSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el impuesto sobre el subtotal.
     *
     * @return valor del IVA
     */
    public BigDecimal calcularImpuesto() {
        return calcularSubtotal().multiply(IVA_COLOMBIA).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Suma el subtotal y el impuesto.
     *
     * @return valor que el cliente debe pagar
     */
    public BigDecimal calcularTotal() {
        return calcularSubtotal().add(calcularImpuesto()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Cuenta las unidades totales, para el contador del encabezado.
     *
     * @return suma de las cantidades de todos los renglones
     */
    public double contarUnidades() {
        double unidades = 0;
        for (ItemCarrito item : items) {
            unidades += item.getCantidad();
        }
        return unidades;
    }

    /**
     * Indica si algun producto del carrito exige formula medica.
     *
     * @return true cuando hay al menos un medicamento controlado
     */
    public boolean requiereFormulaMedica() {
        for (ItemCarrito item : items) {
            if (item.isRequiereReceta()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reune los renglones controlados a los que aun les falta la formula.
     *
     * @return productos que no se pueden comprar todavia
     */
    public List<ItemCarrito> obtenerItemsSinReceta() {
        List<ItemCarrito> pendientes = new ArrayList<>();
        for (ItemCarrito item : items) {
            if (item.faltaReceta()) {
                pendientes.add(item);
            }
        }
        return pendientes;
    }

    /**
     * Indica si el carrito se puede confirmar.
     *
     * @return true cuando algun medicamento controlado no tiene su formula
     */
    public boolean faltanRecetas() {
        return !obtenerItemsSinReceta().isEmpty();
    }

    /**
     * @return true cuando el carrito no tiene renglones
     */
    public boolean estaVacio() {
        return items.isEmpty();
    }

    public List<ItemCarrito> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getCantidadRenglones() {
        return items.size();
    }

    public BigDecimal getSubtotal() {
        return calcularSubtotal();
    }

    public BigDecimal getTotal() {
        return calcularTotal();
    }

    public double getUnidades() {
        return contarUnidades();
    }

    public boolean getVacio() {
        return estaVacio();
    }

    public boolean getConFormulaMedica() {
        return requiereFormulaMedica();
    }

    public boolean getRecetasPendientes() {
        return faltanRecetas();
    }
    /** Subtotal del carrito con separador de miles. */
    public String getSubtotalTexto() {
        return FormatoMoneda.enPesos(calcularSubtotal());
    }

    /** Impuesto del carrito con separador de miles. */
    public String getImpuestoTexto() {
        return FormatoMoneda.enPesos(calcularImpuesto());
    }

    /** Total del carrito con separador de miles. */
    public String getTotalTexto() {
        return FormatoMoneda.enPesos(calcularTotal());
    }

    /** Unidades totales sin decimales, para el contador del encabezado. */
    public String getUnidadesTexto() {
        return FormatoMoneda.enUnidades(contarUnidades());
    }

}

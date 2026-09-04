package co.edu.sena.farmacia.logica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.edu.sena.farmacia.modelo.LineaPedido;
import co.edu.sena.farmacia.modelo.LoteProducto;
import co.edu.sena.farmacia.persistencia.LineaPedidoDao;
import co.edu.sena.farmacia.persistencia.LoteProductoDao;
import co.edu.sena.farmacia.persistencia.ProductoDao;
import co.edu.sena.farmacia.util.FormatoMoneda;

/**
 * Capa de Logica que mueve el inventario cuando un pedido cambia de estado.
 *
 * Un pedido recien confirmado por el cliente queda en PENDIENTE y todavia no
 * toca el inventario: falta que el farmaceutico revise la formula medica. En
 * el momento en que el pedido pasa a un estado en que la mercancia ya salio
 * del estante, las unidades se descuentan de los lotes; si mas adelante el
 * pedido se anula, las unidades regresan.
 *
 * El descuento ocurre una sola vez. Lo que se compara no es el estado nuevo
 * por si solo, sino si el pedido cruzo la frontera entre "todavia esta en el
 * estante" y "ya salio": asi un pedido que recorre CONFIRMADO, EN CAMINO y
 * ENTREGADO descuenta en el primer paso y no en los tres.
 *
 * Las unidades se toman del lote mas proximo a vencer, que es la practica
 * habitual en una farmacia para no dejar vencer la mercancia en el estante.
 */
public class ServicioMovimientoInventario {

    /**
     * Estados en los que la mercancia ya no esta disponible para vender.
     *
     * Se descuenta desde CONFIRMADO y no solo al entregar: cuando el
     * farmaceutico confirma el pedido ya aparto el producto para ese cliente,
     * y dejarlo en el inventario permitiria prometer dos veces la misma
     * unidad.
     */
    public static final List<String> ESTADOS_FUERA_DEL_INVENTARIO =
            Arrays.asList("CONFIRMADO", "EN CAMINO", "ENTREGADO");

    private final LineaPedidoDao lineaPedidoDao = new LineaPedidoDao();
    private final LoteProductoDao loteDao = new LoteProductoDao();
    private final ProductoDao productoDao = new ProductoDao();

    /**
     * Indica si en ese estado el pedido ya salio del inventario.
     *
     * @param estado estado del pedido
     * @return true cuando las unidades ya no deben contar como existencias
     */
    public static boolean saleDelInventario(String estado) {
        if (estado == null) {
            return false;
        }
        return ESTADOS_FUERA_DEL_INVENTARIO.contains(estado.trim().toUpperCase());
    }

    /**
     * Mueve el inventario segun el paso de un estado a otro.
     *
     * @param idPedido identificador del pedido
     * @param estadoAnterior estado que tenia el pedido
     * @param estadoNuevo estado al que pasa
     * @return texto que describe el movimiento, vacio si no hubo ninguno
     */
    public String aplicarCambioDeEstado(int idPedido, String estadoAnterior, String estadoNuevo) {
        boolean estabaFuera = saleDelInventario(estadoAnterior);
        boolean quedaFuera = saleDelInventario(estadoNuevo);

        if (!estabaFuera && quedaFuera) {
            return descontar(idPedido);
        }

        if (estabaFuera && !quedaFuera) {
            return devolver(idPedido);
        }

        return "";
    }

    /**
     * Descuenta del inventario todas las lineas del pedido.
     *
     * Primero comprueba que alcance para todos los productos y solo despues
     * toca los lotes. De esa forma un pedido no queda descontado a medias
     * cuando falta stock del ultimo renglon.
     *
     * @param idPedido identificador del pedido
     * @return texto con las unidades que salieron
     */
    private String descontar(int idPedido) {
        List<LineaPedido> lineas = obtenerLineas(idPedido);
        verificarQueAlcance(lineas);

        double unidades = 0;
        for (LineaPedido linea : lineas) {
            repartirEntreLotes(linea);
            unidades += linea.getCantidad();
        }

        return redactarMovimiento(true, unidades);
    }

    /**
     * Devuelve al inventario todas las lineas del pedido.
     *
     * @param idPedido identificador del pedido
     * @return texto con las unidades que regresaron
     */
    private String devolver(int idPedido) {
        List<LineaPedido> lineas = obtenerLineas(idPedido);

        double unidades = 0;
        for (LineaPedido linea : lineas) {
            loteDao.regresarStock(ubicarLoteParaDevolver(linea).getIdLote(), linea.getCantidad());
            unidades += linea.getCantidad();
        }

        return redactarMovimiento(false, unidades);
    }

    /**
     * Arma el aviso que se le muestra a quien cambio el estado.
     *
     * @param salida true si las unidades salieron, false si regresaron
     * @param unidades cantidad movida
     * @return frase en singular o en plural, segun la cantidad
     */
    private String redactarMovimiento(boolean salida, double unidades) {
        boolean unaSola = unidades == 1;

        String verbo;
        if (salida) {
            verbo = unaSola ? "Salio" : "Salieron";
        } else {
            verbo = unaSola ? "Regreso" : "Regresaron";
        }

        return verbo + " " + FormatoMoneda.enUnidades(unidades)
                + (unaSola ? " unidad " : " unidades ")
                + (salida ? "del inventario." : "al inventario.");
    }

    /**
     * Consulta el detalle del pedido, que es lo que dice cuanto mover.
     */
    private List<LineaPedido> obtenerLineas(int idPedido) {
        List<LineaPedido> lineas = lineaPedidoDao.listarPorPedido(idPedido);

        if (lineas.isEmpty()) {
            throw new ExcepcionNegocio("El pedido " + idPedido
                    + " no tiene productos registrados, asi que no mueve el inventario.");
        }
        return lineas;
    }

    /**
     * Comprueba que haya existencias vigentes para todos los renglones.
     */
    private void verificarQueAlcance(List<LineaPedido> lineas) {
        for (LineaPedido linea : lineas) {
            double disponible = productoDao.consultarStockDisponible(linea.getIdProducto());

            if (disponible < linea.getCantidad()) {
                throw new ExcepcionNegocio("No alcanza el inventario de "
                        + linea.getNombreProducto() + ": el pedido pide "
                        + FormatoMoneda.enUnidades(linea.getCantidad()) + " y hay "
                        + FormatoMoneda.enUnidades(disponible)
                        + " unidades vigentes. Registre un lote antes de cambiar el estado.");
            }
        }
    }

    /**
     * Reparte la cantidad de un renglon entre los lotes vigentes.
     *
     * Empieza por el lote mas proximo a vencer y sigue con el siguiente
     * cuando el primero no alcanza, porque un renglon puede necesitar mas
     * unidades de las que tiene un solo lote.
     */
    private void repartirEntreLotes(LineaPedido linea) {
        double pendiente = linea.getCantidad();

        for (LoteProducto lote : lotesConStock(linea.getIdProducto())) {
            if (pendiente <= 0) {
                return;
            }

            double sale = Math.min(pendiente, lote.getStockActual());
            loteDao.descontarStock(lote.getIdLote(), sale);
            pendiente -= sale;
        }

        // No deberia ocurrir: verificarQueAlcance ya sumo las existencias
        // vigentes de cada producto antes de tocar el primer lote.
        if (pendiente > 0) {
            throw new ExcepcionNegocio("El inventario de " + linea.getNombreProducto()
                    + " cambio mientras se despachaba el pedido. Intentelo de nuevo.");
        }
    }

    /**
     * Elige el lote al que regresan las unidades de un renglon anulado.
     *
     * El detalle del pedido no guarda de que lote salio cada unidad, asi que
     * la devolucion entra al lote vigente mas proximo a vencer. El total por
     * producto queda exacto, que es lo que consultan la tienda y el reporte
     * de inventario.
     */
    private LoteProducto ubicarLoteParaDevolver(LineaPedido linea) {
        List<LoteProducto> vigentes = loteDao.listarVigentesPorProducto(linea.getIdProducto());

        if (vigentes.isEmpty()) {
            throw new ExcepcionNegocio("El producto " + linea.getNombreProducto()
                    + " ya no tiene lotes vigentes, por lo que no hay donde devolver las "
                    + "unidades. Registre un lote en el modulo de Inventario.");
        }
        return vigentes.get(0);
    }

    /**
     * Filtra los lotes vigentes que todavia tienen unidades por despachar.
     */
    private List<LoteProducto> lotesConStock(int idProducto) {
        List<LoteProducto> conStock = new ArrayList<>();

        for (LoteProducto lote : loteDao.listarVigentesPorProducto(idProducto)) {
            if (lote.getStockActual() > 0) {
                conStock.add(lote);
            }
        }
        return conStock;
    }
}

package co.edu.sena.farmacia.logica;

import java.time.LocalDate;
import java.util.List;

import co.edu.sena.farmacia.modelo.Historial;
import co.edu.sena.farmacia.modelo.LineaPedido;
import co.edu.sena.farmacia.modelo.Pedido;
import co.edu.sena.farmacia.modelo.TransaccionPago;
import co.edu.sena.farmacia.persistencia.HistorialDao;
import co.edu.sena.farmacia.persistencia.LineaPedidoDao;
import co.edu.sena.farmacia.persistencia.PedidoDao;
import co.edu.sena.farmacia.persistencia.TransaccionPagoDao;

/**
 * Capa de Logica de la gestion que el propio cliente hace de sus pedidos.
 *
 * Permite modificar las cantidades, retirar productos y cancelar el pedido,
 * siempre que siga en estado PENDIENTE: una vez el farmaceutico lo confirma o
 * lo despacha, el pedido deja de estar en manos del cliente.
 *
 * Toda operacion comprueba primero que el pedido pertenezca a quien lo esta
 * modificando, para que nadie altere el pedido de otra persona cambiando el
 * numero en la direccion.
 */
public class ServicioPedidoCliente {

    /** Estado en el que el cliente todavia puede intervenir su pedido. */
    public static final String ESTADO_MODIFICABLE = "PENDIENTE";

    /** Estado con que queda un pedido cancelado. */
    public static final String ESTADO_CANCELADO = "ANULADO";

    private final PedidoDao pedidoDao = new PedidoDao();
    private final LineaPedidoDao lineaPedidoDao = new LineaPedidoDao();
    private final TransaccionPagoDao transaccionDao = new TransaccionPagoDao();
    private final HistorialDao historialDao = new HistorialDao();

    private final ServicioLineaPedido servicioLinea = new ServicioLineaPedido();
    private final ServicioPedido servicioPedido = new ServicioPedido();

    /**
     * Consulta un pedido comprobando que sea del cliente.
     *
     * @param idPedido pedido solicitado
     * @param idCliente cliente autenticado
     * @return el pedido, si le pertenece
     */
    public Pedido consultarPedido(int idPedido, int idCliente) {
        Pedido pedido = pedidoDao.buscarPorId(idPedido);

        if (pedido == null) {
            throw new ExcepcionNegocio("El pedido solicitado no existe.");
        }
        if (pedido.getIdCliente() != idCliente) {
            throw new ExcepcionNegocio("Ese pedido no le pertenece.");
        }
        return pedido;
    }

    /**
     * Consulta los productos que componen el pedido.
     *
     * @param idPedido pedido a detallar
     * @return lineas del pedido
     */
    public List<LineaPedido> consultarLineas(int idPedido) {
        return lineaPedidoDao.listarPorPedido(idPedido);
    }

    /**
     * Consulta los pagos registrados del pedido.
     *
     * @param idPedido pedido a consultar
     * @return transacciones asociadas
     */
    public List<TransaccionPago> consultarPagos(int idPedido) {
        return transaccionDao.listarPorPedido(idPedido);
    }

    /**
     * Indica si el cliente todavia puede intervenir el pedido.
     *
     * @param pedido pedido a evaluar
     * @return true mientras el pedido siga pendiente
     */
    public boolean puedeModificar(Pedido pedido) {
        return pedido != null && ESTADO_MODIFICABLE.equalsIgnoreCase(pedido.getEstado());
    }

    /**
     * Cambia la cantidad de un producto del pedido.
     *
     * Se delega en la capa de logica del detalle, que valida el inventario,
     * exige la formula medica cuando el producto es controlado y recalcula el
     * total del pedido.
     *
     * @param idPedido pedido a modificar
     * @param idCliente cliente autenticado
     * @param idLinea renglon a cambiar
     * @param cantidad cantidad nueva
     */
    public void cambiarCantidad(int idPedido, int idCliente, int idLinea, double cantidad) {
        Pedido pedido = consultarPedido(idPedido, idCliente);
        verificarQueSeaModificable(pedido);

        LineaPedido linea = servicioLinea.consultarPorId(idLinea);
        verificarQueLaLineaSeaDelPedido(linea, idPedido);

        if (cantidad <= 0) {
            throw new ExcepcionNegocio(
                    "Para dejar el producto en cero, use el boton de retirarlo del pedido.");
        }

        linea.setCantidad(cantidad);
        servicioLinea.modificar(linea);
    }

    /**
     * Retira un producto del pedido.
     *
     * Si era el ultimo, el pedido se cancela: un pedido sin productos no
     * tiene sentido y dejaria un total en cero.
     *
     * @param idPedido pedido a modificar
     * @param idCliente cliente autenticado
     * @param idLinea renglon a retirar
     * @return true si el pedido quedo cancelado por haberse vaciado
     */
    public boolean quitarProducto(int idPedido, int idCliente, int idLinea) {
        Pedido pedido = consultarPedido(idPedido, idCliente);
        verificarQueSeaModificable(pedido);

        LineaPedido linea = servicioLinea.consultarPorId(idLinea);
        verificarQueLaLineaSeaDelPedido(linea, idPedido);

        servicioLinea.eliminar(idLinea);

        if (consultarLineas(idPedido).isEmpty()) {
            cancelar(idPedido, idCliente);
            return true;
        }
        return false;
    }

    /**
     * Cancela el pedido.
     *
     * No se borra: queda en estado ANULADO para conservar la trazabilidad,
     * junto con su registro en el historial. Si el pedido tenia un pago, la
     * devolucion del dinero se gestiona por fuera del sistema.
     *
     * @param idPedido pedido a cancelar
     * @param idCliente cliente autenticado
     */
    public void cancelar(int idPedido, int idCliente) {
        Pedido pedido = consultarPedido(idPedido, idCliente);
        verificarQueSeaModificable(pedido);

        servicioPedido.cambiarEstado(idPedido, ESTADO_CANCELADO);
        registrarEnHistorial(idPedido);
    }

    /**
     * Comprueba que el pedido siga en manos del cliente.
     */
    private void verificarQueSeaModificable(Pedido pedido) {
        if (puedeModificar(pedido)) {
            return;
        }

        if (ESTADO_CANCELADO.equalsIgnoreCase(pedido.getEstado())) {
            throw new ExcepcionNegocio("El pedido " + pedido.getIdPedido()
                    + " ya esta cancelado.");
        }

        throw new ExcepcionNegocio("El pedido " + pedido.getIdPedido() + " esta en estado "
                + pedido.getEstado() + " y ya no se puede modificar. "
                + "Comuniquese con la farmacia.");
    }

    /**
     * Evita que se manipule un renglon que pertenece a otro pedido.
     */
    private void verificarQueLaLineaSeaDelPedido(LineaPedido linea, int idPedido) {
        if (linea.getIdPedido() != idPedido) {
            throw new ExcepcionNegocio("El producto indicado no pertenece a ese pedido.");
        }
    }

    /**
     * Deja constancia de la cancelacion en la bitacora del pedido.
     */
    private void registrarEnHistorial(int idPedido) {
        for (LineaPedido linea : consultarLineas(idPedido)) {
            Historial registro = new Historial();
            registro.setIdPedido(idPedido);
            registro.setIdProducto(linea.getIdProducto());
            registro.setFecha(LocalDate.now());
            registro.setEstado(ESTADO_CANCELADO);

            historialDao.insertar(registro);
        }
    }
}

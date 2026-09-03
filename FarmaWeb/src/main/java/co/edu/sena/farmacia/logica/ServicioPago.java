package co.edu.sena.farmacia.logica;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.edu.sena.farmacia.modelo.PasarelaPago;
import co.edu.sena.farmacia.modelo.Pedido;
import co.edu.sena.farmacia.modelo.TransaccionPago;
import co.edu.sena.farmacia.persistencia.PasarelaPagoDao;
import co.edu.sena.farmacia.persistencia.PedidoDao;
import co.edu.sena.farmacia.persistencia.TransaccionPagoDao;
import co.edu.sena.farmacia.util.ConversorDatos;

/**
 * Capa de Logica del pago del pedido en la tienda.
 *
 * Ofrece al cliente los medios de pago habilitados y registra la
 * transaccion en TblTransaccionPago.
 *
 * La diferencia entre los dos grupos de medios es el estado con que nace la
 * transaccion: el pago en linea por PSE se registra como APROBADA, mientras
 * que la contraentrega queda PENDIENTE porque el dinero se recibe al momento
 * de la entrega. En ambos casos el pedido sigue en estado PENDIENTE hasta que
 * un farmaceutico lo revise.
 */
public class ServicioPago {

    /** Medios de pago que la tienda ofrece al cliente. */
    public static final String PSE = "PSE";
    public static final String CONTRAENTREGA_EFECTIVO = "Contraentrega efectivo";
    public static final String CONTRAENTREGA_DATAFONO = "Contraentrega datafono";

    /** Orden en que se muestran en la pantalla de pago. */
    public static final List<String> MEDIOS_DE_PAGO = Arrays.asList(
            PSE, CONTRAENTREGA_EFECTIVO, CONTRAENTREGA_DATAFONO);

    private final PedidoDao pedidoDao = new PedidoDao();
    private final PasarelaPagoDao pasarelaDao = new PasarelaPagoDao();
    private final TransaccionPagoDao transaccionDao = new TransaccionPagoDao();

    /**
     * Consulta el pedido que se va a pagar, comprobando que sea del cliente.
     *
     * Evita que alguien vea o pague el pedido de otra persona cambiando el
     * numero en la direccion.
     *
     * @param idPedido pedido que se quiere pagar
     * @param idCliente cliente autenticado
     * @return el pedido, si le pertenece al cliente
     */
    public Pedido consultarPedidoDelCliente(int idPedido, int idCliente) {
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
     * Entrega los medios de pago disponibles para la tienda.
     *
     * Solo devuelve los que estan registrados en TblPasarelaPago, de modo que
     * el administrador puede habilitarlos o retirarlos desde su modulo.
     *
     * @return pasarelas correspondientes a PSE y a las dos contraentregas
     */
    public List<PasarelaPago> consultarMediosDisponibles() {
        List<PasarelaPago> disponibles = new ArrayList<>();

        for (String medio : MEDIOS_DE_PAGO) {
            PasarelaPago encontrada = buscarPasarela(medio);
            if (encontrada != null) {
                disponibles.add(encontrada);
            }
        }
        return disponibles;
    }

    /**
     * Indica si ya se registro el pago de un pedido.
     *
     * @param idPedido pedido a consultar
     * @return true cuando el pedido ya tiene transacciones
     */
    public boolean yaTienePago(int idPedido) {
        return !transaccionDao.listarPorPedido(idPedido).isEmpty();
    }

    /**
     * Registra el pago escogido por el cliente.
     *
     * @param idPedido pedido que se paga
     * @param idCliente cliente autenticado
     * @param medioDePago nombre del medio escogido
     * @param montoEntregado valor con que paga el cliente
     * @return el cambio que se le debe devolver, cero si pago exacto
     */
    public BigDecimal registrarPago(int idPedido, int idCliente, String medioDePago,
            BigDecimal montoEntregado) {

        Pedido pedido = consultarPedidoDelCliente(idPedido, idCliente);

        if (yaTienePago(idPedido)) {
            throw new ExcepcionNegocio("El pedido " + idPedido + " ya tiene un pago registrado.");
        }

        validarMedio(medioDePago);
        validarMonto(montoEntregado, pedido.getTotal(), medioDePago);

        PasarelaPago pasarela = buscarPasarela(medioDePago);
        if (pasarela == null) {
            throw new ExcepcionNegocio("El medio de pago " + medioDePago
                    + " no esta habilitado. Pida al administrador que lo registre.");
        }

        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setIdPedido(idPedido);
        transaccion.setIdPasarela(pasarela.getIdPasarela());
        transaccion.setFechaPago(LocalDateTime.now());
        transaccion.setValor(montoEntregado);
        transaccion.setEstadoTransaccion(determinarEstado(medioDePago));

        transaccionDao.insertar(transaccion);

        return calcularCambio(montoEntregado, pedido.getTotal());
    }

    /**
     * Calcula el cambio que se devuelve al cliente.
     *
     * @param montoEntregado valor con que paga
     * @param total valor del pedido
     * @return diferencia, o cero si no hay cambio
     */
    public BigDecimal calcularCambio(BigDecimal montoEntregado, BigDecimal total) {
        if (montoEntregado == null || total == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal cambio = montoEntregado.subtract(total);
        return cambio.compareTo(BigDecimal.ZERO) > 0 ? cambio : BigDecimal.ZERO;
    }

    /**
     * El pago en linea se aprueba de inmediato; la contraentrega queda
     * pendiente hasta que el mensajero reciba el dinero.
     *
     * @param medioDePago medio escogido por el cliente
     * @return estado con que se registra la transaccion
     */
    private String determinarEstado(String medioDePago) {
        return PSE.equals(medioDePago) ? "APROBADA" : "PENDIENTE";
    }

    /**
     * Comprueba que el medio escogido sea uno de los ofrecidos.
     */
    private void validarMedio(String medioDePago) {
        if (ConversorDatos.estaVacio(medioDePago)) {
            throw new ExcepcionNegocio("Debe escoger la forma de pago.");
        }
        if (!MEDIOS_DE_PAGO.contains(medioDePago.trim())) {
            throw new ExcepcionNegocio("La forma de pago " + medioDePago + " no es valida.");
        }
    }

    /**
     * Comprueba que el monto alcance para cubrir el pedido.
     *
     * En el pago por PSE y con datafono se exige el valor exacto, porque en
     * esos medios no hay dinero en efectivo con que dar cambio.
     */
    private void validarMonto(BigDecimal montoEntregado, BigDecimal total, String medioDePago) {
        if (montoEntregado == null || montoEntregado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionNegocio("Debe indicar el monto con que paga.");
        }
        if (montoEntregado.compareTo(total) < 0) {
            throw new ExcepcionNegocio("El monto no alcanza: el pedido vale "
                    + total + " y usted indico " + montoEntregado + ".");
        }

        boolean exigeValorExacto = PSE.equals(medioDePago)
                || CONTRAENTREGA_DATAFONO.equals(medioDePago);

        if (exigeValorExacto && montoEntregado.compareTo(total) > 0) {
            throw new ExcepcionNegocio("Con " + medioDePago
                    + " el monto debe ser exactamente el valor del pedido.");
        }
    }

    /**
     * Busca una pasarela por su nombre de proveedor.
     *
     * @param nombre nombre del medio de pago
     * @return la pasarela encontrada, o null si no esta registrada
     */
    private PasarelaPago buscarPasarela(String nombre) {
        for (PasarelaPago pasarela : pasarelaDao.listar()) {
            String registrado = pasarela.getNombreProveedor();
            if (registrado != null && registrado.trim().equalsIgnoreCase(nombre.trim())) {
                return pasarela;
            }
        }
        return null;
    }
}

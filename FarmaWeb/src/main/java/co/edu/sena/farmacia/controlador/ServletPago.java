package co.edu.sena.farmacia.controlador;

import java.io.IOException;
import java.math.BigDecimal;

import co.edu.sena.farmacia.logica.ExcepcionNegocio;
import co.edu.sena.farmacia.logica.ServicioPago;
import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.logica.ServicioRegistro;
import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Pedido;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.util.ConversorDatos;
import co.edu.sena.farmacia.util.FormatoMoneda;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador del pago del pedido en la tienda.
 *
 * El metodo GET muestra las formas de pago disponibles junto con el valor a
 * pagar, y el metodo POST registra la transaccion escogida.
 *
 * Exige sesion de cliente: el filtro de seguridad ya bloquea el acceso sin
 * cuenta, y aqui se comprueba ademas que el pedido pertenezca a quien lo
 * esta pagando.
 */
@WebServlet(name = "ServletPago", urlPatterns = {"/pago"})
public class ServletPago extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioPago servicioPago = new ServicioPago();
    private final transient ServicioRegistro servicioRegistro = new ServicioRegistro();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);

        try {
            Cliente cliente = obtenerClienteEnSesion(peticion);
            int idPedido = leerEntero(peticion, "idPedido");

            Pedido pedido = servicioPago.consultarPedidoDelCliente(idPedido, cliente.getIdCliente());

            peticion.setAttribute("pedido", pedido);
            peticion.setAttribute("mediosDePago", servicioPago.consultarMediosDisponibles());
            peticion.setAttribute("pagoRegistrado", servicioPago.yaTienePago(idPedido));

            reenviarATienda(peticion, respuesta, "pago.jsp");

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            redirigirAlModulo(peticion, respuesta, "micuenta");
        }
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        int idPedido = 0;

        try {
            Cliente cliente = obtenerClienteEnSesion(peticion);
            idPedido = leerEntero(peticion, "idPedido");

            String medioDePago = peticion.getParameter("medioDePago");
            BigDecimal monto = ConversorDatos.aDecimal(peticion.getParameter("monto"), "monto");

            BigDecimal cambio = servicioPago.registrarPago(
                    idPedido, cliente.getIdCliente(), medioDePago, monto);

            publicarExito(peticion, armarMensaje(idPedido, medioDePago, cambio));
            redirigirAlModulo(peticion, respuesta, "micuenta");

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);

            if (idPedido > 0) {
                respuesta.sendRedirect(peticion.getContextPath() + "/pago?idPedido=" + idPedido);
            } else {
                redirigirAlModulo(peticion, respuesta, "micuenta");
            }
        }
    }

    /**
     * Redacta el mensaje que confirma el pago.
     *
     * Recuerda que el pedido sigue pendiente de revision, porque el pago no
     * reemplaza la validacion del farmaceutico.
     *
     * @param idPedido pedido pagado
     * @param medioDePago medio escogido
     * @param cambio valor a devolver, cero si pago exacto
     * @return texto para mostrar al cliente
     */
    private String armarMensaje(int idPedido, String medioDePago, BigDecimal cambio) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Pago del pedido ").append(idPedido).append(" registrado con ")
               .append(medioDePago).append(". ");

        if (cambio.compareTo(BigDecimal.ZERO) > 0) {
            mensaje.append("Se le devolveran $").append(FormatoMoneda.enPesos(cambio))
                   .append(" de cambio. ");
        }

        if (ServicioPago.PSE.equals(medioDePago)) {
            mensaje.append("El pago quedo aprobado. ");
        } else {
            mensaje.append("El pago se recibe al momento de la entrega. ");
        }

        mensaje.append("Su pedido sigue en estado PENDIENTE hasta que un farmaceutico "
                + "lo revise y lo confirme.");

        return mensaje.toString();
    }

    /**
     * Recupera la ficha de cliente del usuario autenticado.
     *
     * @param peticion peticion HTTP en curso
     * @return cliente en sesion
     */
    private Cliente obtenerClienteEnSesion(HttpServletRequest peticion) {
        Usuario usuario = obtenerUsuario(peticion);

        if (usuario == null) {
            throw new ExcepcionNegocio("Debe ingresar con su cuenta para pagar el pedido.");
        }

        Cliente cliente = servicioRegistro.consultarClienteDeUsuario(usuario.getIdUsuario());
        if (cliente == null) {
            throw new ExcepcionNegocio(
                    "Su cuenta no tiene una ficha de cliente asociada, por lo que no "
                    + "puede pagar pedidos en la tienda.");
        }
        return cliente;
    }
}

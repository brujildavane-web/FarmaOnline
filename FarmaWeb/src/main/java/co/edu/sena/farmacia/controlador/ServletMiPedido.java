package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ExcepcionNegocio;
import co.edu.sena.farmacia.logica.ServicioPedidoCliente;
import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.logica.ServicioRegistro;
import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Pedido;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.util.ConversorDatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador con que el cliente gestiona uno de sus pedidos.
 *
 * El metodo GET muestra el detalle del pedido con su estado y sus pagos, y el
 * metodo POST atiende las tres acciones que el cliente puede realizar
 * mientras el pedido siga pendiente: cambiar una cantidad, retirar un
 * producto y cancelar el pedido.
 *
 * Todas las operaciones comprueban que el pedido sea del cliente en sesion.
 */
@WebServlet(name = "ServletMiPedido", urlPatterns = {"/mipedido"})
public class ServletMiPedido extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioPedidoCliente servicioPedidoCliente = new ServicioPedidoCliente();
    private final transient ServicioRegistro servicioRegistro = new ServicioRegistro();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);

        try {
            Cliente cliente = obtenerClienteEnSesion(peticion);
            int idPedido = leerEntero(peticion, "id");

            Pedido pedido = servicioPedidoCliente.consultarPedido(idPedido, cliente.getIdCliente());

            peticion.setAttribute("pedido", pedido);
            peticion.setAttribute("lineas", servicioPedidoCliente.consultarLineas(idPedido));
            peticion.setAttribute("pagos", servicioPedidoCliente.consultarPagos(idPedido));
            peticion.setAttribute("modificable", servicioPedidoCliente.puedeModificar(pedido));

            reenviarATienda(peticion, respuesta, "mipedido.jsp");

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            redirigirAlModulo(peticion, respuesta, "micuenta");
        }
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        int idPedido = 0;
        boolean volverACuenta = false;

        try {
            Cliente cliente = obtenerClienteEnSesion(peticion);
            idPedido = leerEntero(peticion, "idPedido");

            String accion = leerAccion(peticion, "actualizar");

            if ("cancelar".equals(accion)) {
                servicioPedidoCliente.cancelar(idPedido, cliente.getIdCliente());
                publicarExito(peticion, armarMensajeDeCancelacion(idPedido));
                volverACuenta = true;

            } else if ("quitar".equals(accion)) {
                boolean quedoCancelado = servicioPedidoCliente.quitarProducto(
                        idPedido, cliente.getIdCliente(), leerEntero(peticion, "idLinea"));

                if (quedoCancelado) {
                    publicarExito(peticion, "Retiró el último producto, así que el pedido "
                            + idPedido + " quedó cancelado. " + armarMensajeDeCancelacion(idPedido));
                    volverACuenta = true;
                } else {
                    publicarExito(peticion, "El producto fue retirado y el total se recalculó.");
                }

            } else {
                servicioPedidoCliente.cambiarCantidad(idPedido, cliente.getIdCliente(),
                        leerEntero(peticion, "idLinea"),
                        ConversorDatos.aDouble(peticion.getParameter("cantidad"), "cantidad"));
                publicarExito(peticion, "La cantidad fue actualizada y el total se recalculó.");
            }

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        if (volverACuenta || idPedido == 0) {
            redirigirAlModulo(peticion, respuesta, "micuenta");
        } else {
            respuesta.sendRedirect(peticion.getContextPath() + "/mipedido?id=" + idPedido);
        }
    }

    /**
     * Redacta el aviso de cancelacion, advirtiendo sobre la devolucion del
     * dinero cuando el pedido ya tenia un pago registrado.
     *
     * @param idPedido pedido cancelado
     * @return texto para mostrar al cliente
     */
    private String armarMensajeDeCancelacion(int idPedido) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("El pedido ").append(idPedido).append(" quedó cancelado. ");

        if (!servicioPedidoCliente.consultarPagos(idPedido).isEmpty()) {
            mensaje.append("Como ya tenía un pago registrado: si pagó por PSE, ")
                   .append("el reembolso dependerá de su entidad bancaria y de sus tiempos; ")
                   .append("si eligió contraentrega, no se le cobrará nada.");
        }
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
            throw new ExcepcionNegocio("Debe ingresar con su cuenta para gestionar sus pedidos.");
        }

        Cliente cliente = servicioRegistro.consultarClienteDeUsuario(usuario.getIdUsuario());
        if (cliente == null) {
            throw new ExcepcionNegocio(
                    "Su cuenta no tiene una ficha de cliente asociada, por lo que no "
                    + "puede gestionar pedidos.");
        }
        return cliente;
    }
}

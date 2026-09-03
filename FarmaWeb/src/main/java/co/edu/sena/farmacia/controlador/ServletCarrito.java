package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ExcepcionNegocio;
import co.edu.sena.farmacia.logica.ServicioCarrito;
import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.logica.ServicioRegistro;
import co.edu.sena.farmacia.modelo.Carrito;
import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.util.AlmacenRecetas;
import co.edu.sena.farmacia.util.ConversorDatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Controlador del carrito de compras.
 *
 * El metodo GET muestra el contenido del carrito y el metodo POST atiende
 * las acciones que lo modifican: agregar, actualizar, quitar, vaciar y
 * confirmar el pedido.
 *
 * El carrito es publico: se puede armar sin cuenta. Solo la confirmacion
 * exige que el visitante se haya registrado o haya ingresado.
 *
 * La anotacion MultipartConfig habilita la recepcion de archivos, que se
 * necesita para adjuntar la formula medica de los medicamentos controlados.
 */
@WebServlet(name = "ServletCarrito", urlPatterns = {"/carrito"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = AlmacenRecetas.TAMANO_MAXIMO_BYTES,
        maxRequestSize = AlmacenRecetas.TAMANO_MAXIMO_BYTES * 2)
public class ServletCarrito extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioCarrito servicioCarrito = new ServicioCarrito();
    private final transient ServicioRegistro servicioRegistro = new ServicioRegistro();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("usuario", obtenerUsuario(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);

        reenviarATienda(peticion, respuesta, "carrito.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        Carrito carrito = obtenerCarrito(peticion);
        String accion = leerAccion(peticion, "agregar");

        try {
            if ("agregar".equals(accion)) {
                agregar(peticion, carrito);

            } else if ("actualizar".equals(accion)) {
                servicioCarrito.cambiarCantidad(carrito,
                        leerEntero(peticion, "idProducto"),
                        ConversorDatos.aDouble(peticion.getParameter("cantidad"), "cantidad"));
                publicarExito(peticion, "El carrito fue actualizado.");

            } else if ("quitar".equals(accion)) {
                carrito.quitar(leerEntero(peticion, "idProducto"));
                publicarExito(peticion, "El producto fue retirado del carrito.");

            } else if ("vaciar".equals(accion)) {
                carrito.vaciar();
                publicarExito(peticion, "Su carrito quedo vacio.");

            } else if ("adjuntarreceta".equals(accion)) {
                adjuntarReceta(peticion, carrito);

            } else if ("quitarreceta".equals(accion)) {
                servicioCarrito.quitarReceta(carrito, leerEntero(peticion, "idProducto"));
                publicarExito(peticion, "La formula medica fue retirada. Adjunte otra para continuar.");

            } else if ("confirmar".equals(accion)) {
                confirmar(peticion, respuesta, carrito);
                return;
            }

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        redirigirAlModulo(peticion, respuesta, "carrito");
    }

    /**
     * Agrega un producto y devuelve al visitante a donde estaba.
     *
     * @param peticion peticion HTTP con los datos enviados por POST
     * @param carrito carrito de la sesion
     */
    private void agregar(HttpServletRequest peticion, Carrito carrito) {
        int idProducto = leerEntero(peticion, "idProducto");

        String cantidadDigitada = peticion.getParameter("cantidad");
        double cantidad = ConversorDatos.estaVacio(cantidadDigitada)
                ? 1
                : ConversorDatos.aDouble(cantidadDigitada, "cantidad");

        String nombre = servicioCarrito.agregarProducto(carrito, idProducto, cantidad);
        publicarExito(peticion, nombre + " se agrego a su carrito.");
    }

    /**
     * Recibe el archivo de la formula medica y lo asocia al renglon.
     *
     * @param peticion peticion multipart con el archivo y la fecha
     * @param carrito carrito de la sesion
     */
    private void adjuntarReceta(HttpServletRequest peticion, Carrito carrito)
            throws IOException, ServletException {

        int idProducto = leerEntero(peticion, "idProducto");
        Part archivo = peticion.getPart("archivoReceta");

        String carpeta = getServletContext().getInitParameter("carpetaRecetas");
        String nombreGuardado = AlmacenRecetas.guardar(archivo, idProducto, carpeta);

        String nombreProducto = servicioCarrito.adjuntarReceta(carrito, idProducto, nombreGuardado,
                ConversorDatos.aFecha(peticion.getParameter("fechaPrescripcion"),
                        "fecha de la formula"));

        publicarExito(peticion, "La formula medica de " + nombreProducto
                + " quedo adjunta. Ya puede confirmar su pedido.");
    }

    /**
     * Confirma la compra si hay sesion; si no, lleva al registro.
     *
     * @param peticion peticion HTTP en curso
     * @param respuesta respuesta HTTP en curso
     * @param carrito carrito de la sesion
     */
    private void confirmar(HttpServletRequest peticion, HttpServletResponse respuesta,
            Carrito carrito) throws IOException {

        Usuario usuario = obtenerUsuario(peticion);

        if (usuario == null) {
            publicarError(peticion, "Cree su cuenta o ingrese para confirmar el pedido.");
            redirigirAlModulo(peticion, respuesta, "registro");
            return;
        }

        try {
            Cliente cliente = servicioRegistro.consultarClienteDeUsuario(usuario.getIdUsuario());
            if (cliente == null) {
                throw new ExcepcionNegocio(
                        "Su cuenta no tiene una ficha de cliente asociada. "
                        + "Solo los clientes pueden confirmar pedidos en la tienda.");
            }

            int idPedido = servicioCarrito.confirmarPedido(carrito, cliente.getIdCliente());

            // El paso siguiente es escoger la forma de pago
            respuesta.sendRedirect(peticion.getContextPath() + "/pago?idPedido=" + idPedido);

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            redirigirAlModulo(peticion, respuesta, "carrito");
        }
    }
}

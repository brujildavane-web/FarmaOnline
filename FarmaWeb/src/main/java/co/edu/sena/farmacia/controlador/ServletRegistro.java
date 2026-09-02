package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.logica.ServicioRegistro;
import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.util.ConversorDatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controlador del registro de clientes en la tienda publica.
 *
 * El metodo GET muestra el formulario y el metodo POST crea la cuenta. Toda
 * persona que se registra aqui recibe el rol Cliente; los perfiles de
 * Farmaceutico y Administrador solo los asigna el administrador desde el
 * modulo de Usuarios.
 *
 * Al terminar el registro la sesion queda abierta, para que el visitante
 * pueda confirmar de inmediato el pedido que ya tenia en su carrito.
 */
@WebServlet(name = "ServletRegistro", urlPatterns = {"/registro"})
public class ServletRegistro extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioRegistro servicioRegistro = new ServicioRegistro();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        if (existeSesionActiva(peticion)) {
            redirigirAlModulo(peticion, respuesta, "micuenta");
            return;
        }

        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);
        peticion.setAttribute("rolAsignado", ServicioRegistro.ROL_CLIENTE);

        reenviarATienda(peticion, respuesta, "registro.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        try {
            Cliente cliente = armarCliente(peticion);

            Usuario creado = servicioRegistro.registrarCliente(
                    cliente,
                    peticion.getParameter("password"),
                    peticion.getParameter("password2"));

            HttpSession sesion = peticion.getSession(true);
            sesion.setAttribute(ATRIBUTO_USUARIO, creado);

            publicarExito(peticion, "Su cuenta fue creada con el rol "
                    + ServicioRegistro.ROL_CLIENTE + ". Bienvenido "
                    + cliente.getNombre() + ".");

            redirigirAlModulo(peticion, respuesta, "micuenta");

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            conservarDatosDigitados(peticion);
            redirigirAlModulo(peticion, respuesta, "registro");
        }
    }

    /**
     * Arma el cliente con los datos que llegaron por POST.
     *
     * @param peticion peticion HTTP con el formulario enviado
     * @return cliente sin identificador, listo para registrar
     */
    private Cliente armarCliente(HttpServletRequest peticion) {
        Cliente cliente = new Cliente();
        cliente.setNombre(ConversorDatos.limpiar(peticion.getParameter("nombre")));
        cliente.setApellido(ConversorDatos.limpiar(peticion.getParameter("apellido")));
        cliente.setEmail(ConversorDatos.limpiar(peticion.getParameter("email")));
        cliente.setTelefono(ConversorDatos.limpiar(peticion.getParameter("telefono")));
        cliente.setDireccion(ConversorDatos.limpiar(peticion.getParameter("direccion")));
        cliente.setFechaNacimiento(ConversorDatos.aFechaOpcional(
                peticion.getParameter("fechaNacimiento"), "fecha de nacimiento"));
        return cliente;
    }

    /**
     * Guarda en la sesion lo que la persona ya habia escrito, para no
     * obligarla a digitar todo de nuevo cuando la validacion falla.
     *
     * @param peticion peticion HTTP en curso
     */
    private void conservarDatosDigitados(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(true);
        sesion.setAttribute("registroNombre", peticion.getParameter("nombre"));
        sesion.setAttribute("registroApellido", peticion.getParameter("apellido"));
        sesion.setAttribute("registroEmail", peticion.getParameter("email"));
        sesion.setAttribute("registroTelefono", peticion.getParameter("telefono"));
        sesion.setAttribute("registroDireccion", peticion.getParameter("direccion"));
        sesion.setAttribute("registroFechaNacimiento", peticion.getParameter("fechaNacimiento"));
    }
}

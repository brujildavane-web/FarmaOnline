package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ServicioUsuario;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.util.ConexionBaseDatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controlador del ingreso al sistema.
 *
 * El metodo GET muestra el formulario de autenticacion y el metodo POST
 * recibe las credenciales digitadas por el usuario.
 */
@WebServlet(name = "ServletLogin", urlPatterns = {"/login"})
public class ServletLogin extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioUsuario servicioUsuario = new ServicioUsuario();

    /**
     * Muestra el formulario de ingreso.
     *
     * Antes de dibujarlo comprueba que la base de datos responda, para que el
     * usuario sepa de inmediato si el motor MySQL esta apagado.
     */
    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        if (existeSesionActiva(peticion)) {
            redirigirSegunRol(peticion, respuesta, obtenerUsuario(peticion));
            return;
        }

        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("baseDatosDisponible", ConexionBaseDatos.probarConexion());
        peticion.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(peticion, respuesta);
    }

    /**
     * Recibe el correo y la contrasena enviados por el formulario y abre la
     * sesion cuando las credenciales son correctas.
     */
    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        String email = peticion.getParameter("email");
        String password = peticion.getParameter("password");

        try {
            Usuario usuarioAutenticado = servicioUsuario.autenticar(email, password);

            HttpSession sesion = peticion.getSession(true);
            sesion.setAttribute(ATRIBUTO_USUARIO, usuarioAutenticado);

            publicarExito(peticion, "Bienvenido " + usuarioAutenticado.getEmail() + ".");
            redirigirSegunRol(peticion, respuesta, usuarioAutenticado);

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            peticion.setAttribute("emailDigitado", email);
            peticion.setAttribute("carrito", obtenerCarrito(peticion));
            peticion.setAttribute("baseDatosDisponible", ConexionBaseDatos.probarConexion());
            peticion.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(peticion, respuesta);
        }
    }

    /**
     * Lleva al usuario a la pantalla que corresponde a su perfil.
     *
     * Administrador y Farmaceutico entran al panel de gestion; el Cliente
     * entra a su cuenta, donde puede confirmar la compra que dejo en el
     * carrito antes de identificarse.
     *
     * @param peticion peticion HTTP en curso
     * @param respuesta respuesta HTTP en curso
     * @param usuario usuario autenticado
     */
    private void redirigirSegunRol(HttpServletRequest peticion, HttpServletResponse respuesta,
            Usuario usuario) throws IOException {

        boolean esPerfilDeGestion = usuario != null
                && (usuario.esAdministrador() || usuario.esFarmaceutico());

        redirigirAlModulo(peticion, respuesta, esPerfilDeGestion ? "panel" : "micuenta");
    }
}

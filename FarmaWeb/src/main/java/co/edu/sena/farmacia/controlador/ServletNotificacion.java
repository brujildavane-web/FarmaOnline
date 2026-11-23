package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ServicioNotificacion;
import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.modelo.Notificacion;
import co.edu.sena.farmacia.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controlador de las notificaciones del usuario autenticado.
 *
 * El metodo GET muestra los avisos del usuario y los marca como leidos; el
 * metodo POST con la accion leer solo los marca. Cada persona ve unicamente
 * sus propios avisos porque la consulta se hace con el usuario de la sesion,
 * nunca con un identificador recibido por la direccion.
 */
@WebServlet(name = "ServletNotificacion", urlPatterns = {"/notificaciones"})
public class ServletNotificacion extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioNotificacion servicioNotificacion = new ServicioNotificacion();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuario(peticion);

        if (usuario == null) {
            redirigirAlModulo(peticion, respuesta, "login");
            return;
        }

        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);

        try {
            List<Notificacion> avisos = servicioNotificacion.consultar(usuario.getIdUsuario());
            peticion.setAttribute("notificaciones", avisos);

            // Se muestra el estado previo (nuevas resaltadas) y despues se marcan
            servicioNotificacion.marcarTodasLeidas(usuario.getIdUsuario());
            peticion.setAttribute("notificacionesSinLeer", 0);

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        reenviarATienda(peticion, respuesta, "notificaciones.jsp");
    }
}

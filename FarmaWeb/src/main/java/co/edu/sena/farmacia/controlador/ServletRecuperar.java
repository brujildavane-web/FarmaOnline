package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ServicioRecuperacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador de la recuperacion de contrasena.
 *
 * Atiende dos rutas publicas:
 *
 *   /recuperar    la persona escribe su correo y el sistema genera el enlace.
 *   /restablecer  la persona abre el enlace y escribe su contrasena nueva.
 *
 * La respuesta a la solicitud es siempre la misma exista o no el correo, para
 * que nadie pueda averiguar que correos estan registrados. El enlace se
 * muestra en pantalla porque el proyecto no tiene servidor de correo: es una
 * simulacion declarada del mensaje que recibiria la persona.
 */
@WebServlet(name = "ServletRecuperar", urlPatterns = {"/recuperar", "/restablecer"})
public class ServletRecuperar extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioRecuperacion servicioRecuperacion = new ServicioRecuperacion();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        if ("/restablecer".equals(peticion.getServletPath())) {
            mostrarRestablecer(peticion, respuesta);
        } else {
            mostrarSolicitud(peticion, respuesta);
        }
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        if ("/restablecer".equals(peticion.getServletPath())) {
            procesarRestablecer(peticion, respuesta);
        } else {
            procesarSolicitud(peticion, respuesta);
        }
    }

    private void mostrarSolicitud(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        peticion.getRequestDispatcher("/WEB-INF/jsp/recuperar.jsp").forward(peticion, respuesta);
    }

    private void procesarSolicitud(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        String email = peticion.getParameter("email");
        peticion.setAttribute("emailDigitado", email);

        try {
            String codigo = servicioRecuperacion.solicitar(email);

            peticion.setAttribute("solicitudProcesada", true);
            peticion.setAttribute("minutosVigencia", ServicioRecuperacion.MINUTOS_VIGENCIA);

            if (codigo != null) {
                peticion.setAttribute("enlaceSimulado", peticion.getContextPath()
                        + "/restablecer?codigo=" + codigo);
            }

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        mostrarSolicitud(peticion, respuesta);
    }

    private void mostrarRestablecer(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        String codigo = peticion.getParameter("codigo");

        try {
            peticion.setAttribute("enlaceVigente", servicioRecuperacion.esEnlaceVigente(codigo));
        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            peticion.setAttribute("enlaceVigente", false);
        }

        peticion.setAttribute("codigo", codigo);
        peticion.getRequestDispatcher("/WEB-INF/jsp/restablecer.jsp").forward(peticion, respuesta);
    }

    private void procesarRestablecer(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        String codigo = peticion.getParameter("codigo");

        try {
            servicioRecuperacion.restablecer(codigo,
                    peticion.getParameter("password"),
                    peticion.getParameter("password2"));

            publicarExito(peticion,
                    "Su contrasena fue cambiada. Ya puede ingresar con la contrasena nueva.");
            redirigirAlModulo(peticion, respuesta, "login");

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            mostrarRestablecer(peticion, respuesta);
        }
    }
}

package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ExcepcionNegocio;
import co.edu.sena.farmacia.modelo.Carrito;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.persistencia.ExcepcionPersistencia;
import co.edu.sena.farmacia.util.ConversorDatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Clase padre de todos los servlets del sistema.
 *
 * Reune el comportamiento que comparten los trece modulos: reenviar la
 * peticion a una pagina JSP, publicar mensajes de exito o error y traducir
 * las excepciones a un texto entendible para el usuario.
 *
 * Aplica el principio de herencia para no repetir este codigo en cada
 * controlador del proyecto.
 */
public abstract class ServletBase extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Carpeta donde viven las paginas JSP de los modulos de gestion. */
    protected static final String RUTA_VISTAS = "/WEB-INF/jsp/modulos/";

    /** Carpeta donde viven las paginas JSP de la tienda publica. */
    protected static final String RUTA_VISTAS_TIENDA = "/WEB-INF/jsp/tienda/";

    /** Nombre con que se guarda el carrito de compras en la sesion. */
    public static final String ATRIBUTO_CARRITO = "carritoSesion";

    /** Nombre con que se guarda el usuario autenticado en la sesion. */
    public static final String ATRIBUTO_USUARIO = "usuarioSesion";

    /** Nombres de los mensajes que la cabecera JSP muestra y luego borra. */
    public static final String ATRIBUTO_MENSAJE_EXITO = "mensajeExito";
    public static final String ATRIBUTO_MENSAJE_ERROR = "mensajeError";

    /**
     * Entrega el control a una pagina JSP para que dibuje la interfaz.
     *
     * @param peticion peticion HTTP en curso
     * @param respuesta respuesta HTTP en curso
     * @param nombreVista nombre del archivo JSP dentro de la carpeta de modulos
     */
    protected void reenviarAVista(HttpServletRequest peticion, HttpServletResponse respuesta,
            String nombreVista) throws ServletException, IOException {
        peticion.getRequestDispatcher(RUTA_VISTAS + nombreVista).forward(peticion, respuesta);
    }

    /**
     * Guarda un mensaje de exito que se mostrara en la siguiente pantalla.
     *
     * @param peticion peticion HTTP en curso
     * @param mensaje texto a mostrar al usuario
     */
    protected void publicarExito(HttpServletRequest peticion, String mensaje) {
        peticion.getSession().setAttribute(ATRIBUTO_MENSAJE_EXITO, mensaje);
    }

    /**
     * Guarda un mensaje de error que se mostrara en la siguiente pantalla.
     *
     * @param peticion peticion HTTP en curso
     * @param mensaje texto a mostrar al usuario
     */
    protected void publicarError(HttpServletRequest peticion, String mensaje) {
        peticion.getSession().setAttribute(ATRIBUTO_MENSAJE_ERROR, mensaje);
    }

    /**
     * Traduce una excepcion al mensaje que se muestra en la interfaz.
     *
     * Los errores de negocio se muestran tal cual porque estan redactados
     * para el usuario; los errores tecnicos se resumen sin exponer detalles
     * internos del motor de base de datos.
     *
     * @param peticion peticion HTTP en curso
     * @param error excepcion capturada
     */
    protected void publicarErrorDeExcepcion(HttpServletRequest peticion, RuntimeException error) {
        if (error instanceof ExcepcionNegocio) {
            publicarError(peticion, error.getMessage());
        } else if (error instanceof ExcepcionPersistencia) {
            publicarError(peticion, error.getMessage());
            log("Error de persistencia en " + getClass().getSimpleName(), error);
        } else {
            publicarError(peticion, "Ocurrio un error inesperado al procesar la solicitud.");
            log("Error inesperado en " + getClass().getSimpleName(), error);
        }
    }

    /**
     * Aplica el patron peticion-redireccion para que al recargar el navegador
     * no se repita el envio del formulario.
     *
     * @param peticion peticion HTTP en curso
     * @param respuesta respuesta HTTP en curso
     * @param rutaModulo ruta del modulo, por ejemplo "productos"
     */
    protected void redirigirAlModulo(HttpServletRequest peticion, HttpServletResponse respuesta,
            String rutaModulo) throws IOException {
        respuesta.sendRedirect(peticion.getContextPath() + "/" + rutaModulo);
    }

    /**
     * Lee un parametro obligatorio de la peticion y lo convierte a entero.
     *
     * @param peticion peticion HTTP en curso
     * @param nombreParametro nombre del parametro enviado por GET o POST
     * @return valor numerico del parametro
     */
    protected int leerEntero(HttpServletRequest peticion, String nombreParametro) {
        return ConversorDatos.aEntero(peticion.getParameter(nombreParametro), nombreParametro);
    }

    /**
     * Lee un parametro numerico opcional de la peticion.
     *
     * @param peticion peticion HTTP en curso
     * @param nombreParametro nombre del parametro
     * @return valor numerico, o 0 si el parametro no fue enviado
     */
    protected int leerEnteroOpcional(HttpServletRequest peticion, String nombreParametro) {
        String valor = peticion.getParameter(nombreParametro);
        return ConversorDatos.estaVacio(valor) ? 0 : ConversorDatos.aEntero(valor, nombreParametro);
    }

    /**
     * Lee la accion solicitada por el usuario.
     *
     * @param peticion peticion HTTP en curso
     * @param accionPorDefecto accion a usar si el parametro no viene
     * @return nombre de la accion en minusculas
     */
    protected String leerAccion(HttpServletRequest peticion, String accionPorDefecto) {
        String accion = peticion.getParameter("accion");
        return ConversorDatos.estaVacio(accion) ? accionPorDefecto : accion.trim().toLowerCase();
    }

    /**
     * Entrega el control a una pagina JSP de la tienda publica.
     *
     * @param peticion peticion HTTP en curso
     * @param respuesta respuesta HTTP en curso
     * @param nombreVista nombre del archivo JSP dentro de la carpeta de tienda
     */
    protected void reenviarATienda(HttpServletRequest peticion, HttpServletResponse respuesta,
            String nombreVista) throws ServletException, IOException {
        peticion.getRequestDispatcher(RUTA_VISTAS_TIENDA + nombreVista).forward(peticion, respuesta);
    }

    /**
     * Recupera el carrito de la sesion y lo crea si es la primera visita.
     *
     * El carrito no se guarda en la base de datos: vive en la sesion hasta
     * que el cliente confirma el pedido.
     *
     * @param peticion peticion HTTP en curso
     * @return carrito del visitante, nunca nulo
     */
    protected Carrito obtenerCarrito(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(true);
        Carrito carrito = (Carrito) sesion.getAttribute(ATRIBUTO_CARRITO);

        if (carrito == null) {
            carrito = new Carrito();
            sesion.setAttribute(ATRIBUTO_CARRITO, carrito);
        }
        return carrito;
    }

    /**
     * Recupera el usuario autenticado, si lo hay.
     *
     * @param peticion peticion HTTP en curso
     * @return el usuario en sesion, o null cuando se navega como visitante
     */
    protected Usuario obtenerUsuario(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion == null) {
            return null;
        }
        return (Usuario) sesion.getAttribute(ATRIBUTO_USUARIO);
    }

    /**
     * Consulta si hay un usuario autenticado en la sesion.
     *
     * @param peticion peticion HTTP en curso
     * @return true cuando existe sesion activa
     */
    protected boolean existeSesionActiva(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        return sesion != null && sesion.getAttribute(ATRIBUTO_USUARIO) != null;
    }
}

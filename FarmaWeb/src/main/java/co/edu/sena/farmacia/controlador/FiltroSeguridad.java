package co.edu.sena.farmacia.controlador;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.edu.sena.farmacia.logica.CatalogoModulos;
import co.edu.sena.farmacia.logica.ServicioNotificacion;
import co.edu.sena.farmacia.modelo.ModuloSistema;
import co.edu.sena.farmacia.modelo.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controla quien puede entrar a cada zona del sistema.
 *
 * El sitio tiene tres zonas:
 *
 *   Publica       la tienda: inicio, catalogo, detalle de producto, carrito,
 *                 ingreso y registro. No exige cuenta.
 *   Del cliente   mi cuenta, la gestion de sus pedidos, el pago, la salida
 *                 al portal de PSE y la confirmacion de la compra. Exige
 *                 sesion abierta.
 *   De gestion    el tablero, los modulos del modelo relacional y los
 *                 reportes. Exige sesion y ademas que el rol tenga la
 *                 pestana habilitada en el CatalogoModulos.
 *
 * La verificacion del rol es indispensable: sin ella un cliente autenticado
 * podria abrir el modulo de usuarios simplemente escribiendo la direccion.
 *
 * El permiso se consulta modulo por modulo en el CatalogoModulos, que es la
 * misma fuente que dibuja el menu y que el administrador consulta en el
 * modulo de Roles. Asi lo que se muestra en pantalla y lo que se autoriza
 * nunca pueden quedar desalineados.
 */
@WebFilter("/*")
public class FiltroSeguridad implements Filter {

    /** Nombre con que el menu de la cabecera recibe las pestanas del rol. */
    public static final String ATRIBUTO_MENU = "modulosDelMenu";

    /** Rutas que cualquiera puede visitar, con o sin cuenta. */
    private static final List<String> RUTAS_PUBLICAS = Arrays.asList(
            "/", "/inicio", "/catalogo", "/producto", "/carrito",
            "/login", "/logout", "/registro", "/recuperar", "/restablecer",
            "/css/", "/img/", "/favicon.ico");

    /** Nombre con que las cabeceras reciben la cantidad de avisos sin leer. */
    public static final String ATRIBUTO_SIN_LEER = "notificacionesSinLeer";

    private final ServicioNotificacion servicioNotificacion = new ServicioNotificacion();

    @Override
    public void doFilter(ServletRequest peticionGenerica, ServletResponse respuestaGenerica,
            FilterChain cadena) throws IOException, ServletException {

        HttpServletRequest peticion = (HttpServletRequest) peticionGenerica;
        HttpServletResponse respuesta = (HttpServletResponse) respuestaGenerica;

        String ruta = peticion.getRequestURI().substring(peticion.getContextPath().length());
        Usuario usuario = obtenerUsuarioEnSesion(peticion);

        // El menu se arma antes de atender la peticion para que la cabecera
        // dibuje unicamente las pestanas habilitadas al rol en sesion.
        if (usuario != null) {
            peticion.setAttribute(ATRIBUTO_MENU, CatalogoModulos.modulosDelRol(
                    usuario.getNombreRol(), CatalogoModulos.ZONA_GESTION));
        }

        // El indicador de notificaciones se calcula solo para paginas, no para
        // las hojas de estilo ni las imagenes.
        if (usuario != null && !ruta.startsWith("/css/") && !ruta.startsWith("/img/")) {
            peticion.setAttribute(ATRIBUTO_SIN_LEER,
                    servicioNotificacion.contarSinLeer(usuario.getIdUsuario()));
        }

        // Zona publica: pasa siempre
        if (esRutaPublica(ruta)) {
            cadena.doFilter(peticion, respuesta);
            return;
        }

        // Cualquier otra zona exige sesion abierta
        if (usuario == null) {
            respuesta.sendRedirect(peticion.getContextPath() + "/login");
            return;
        }

        // Zona de gestion: exige que el rol tenga la pestana habilitada
        ModuloSistema modulo = CatalogoModulos.buscarPorRuta(ruta);

        if (esModuloDeGestion(modulo) && !modulo.tieneAcceso(usuario.getNombreRol())) {
            peticion.getSession().setAttribute(ServletBase.ATRIBUTO_MENSAJE_ERROR,
                    "El perfil " + usuario.getNombreRol() + " no tiene habilitada la pestana "
                            + modulo.getEtiqueta() + ".");
            respuesta.sendRedirect(peticion.getContextPath() + elegirDestinoPermitido(usuario));
            return;
        }

        cadena.doFilter(peticion, respuesta);
    }

    /**
     * Determina si la ruta pertenece a la tienda publica.
     *
     * @param ruta ruta pedida por el navegador
     * @return true cuando no se necesita cuenta
     */
    private boolean esRutaPublica(String ruta) {
        if (ruta.isEmpty()) {
            return true;
        }

        for (String publica : RUTAS_PUBLICAS) {
            if (ruta.equals(publica)) {
                return true;
            }

            // Solo las carpetas de recursos actuan como prefijo. La raiz "/"
            // se compara unicamente de forma exacta: si se tratara como
            // prefijo, cualquier ruta del sistema quedaria abierta.
            boolean esCarpetaDeRecursos = publica.length() > 1 && publica.endsWith("/");

            if (esCarpetaDeRecursos && ruta.startsWith(publica)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica que la ruta corresponda a un modulo del panel de gestion.
     *
     * @param modulo modulo hallado en el catalogo, o null
     * @return true cuando la ruta pertenece a la zona de gestion
     */
    private boolean esModuloDeGestion(ModuloSistema modulo) {
        return modulo != null && CatalogoModulos.ZONA_GESTION.equals(modulo.getZona());
    }

    /**
     * Elige a donde devolver al usuario cuando pidio un modulo que su rol
     * no tiene habilitado.
     *
     * Quien conserva pestanas de gestion vuelve al tablero; quien no tiene
     * ninguna, como el cliente, vuelve a su cuenta. Se comprueba el acceso
     * al tablero antes de proponerlo para no provocar un reenvio en ciclo.
     *
     * @param usuario usuario autenticado
     * @return ruta a la que se puede reenviar sin volver a ser rechazado
     */
    private String elegirDestinoPermitido(Usuario usuario) {
        ModuloSistema tablero = CatalogoModulos.buscarPorRuta("/panel");

        if (tablero != null && tablero.tieneAcceso(usuario.getNombreRol())) {
            return "/panel";
        }
        return "/micuenta";
    }

    /**
     * Recupera el usuario autenticado sin crear una sesion nueva.
     *
     * @param peticion peticion HTTP en curso
     * @return el usuario en sesion, o null si no hay sesion activa
     */
    private Usuario obtenerUsuarioEnSesion(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion == null) {
            return null;
        }
        return (Usuario) sesion.getAttribute(ServletBase.ATRIBUTO_USUARIO);
    }
}

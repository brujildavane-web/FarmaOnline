package co.edu.sena.farmacia.controlador;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
 *   Del cliente   mi cuenta, el pago del pedido y la confirmacion de la
 *                 compra. Exige sesion abierta.
 *   De gestion    el tablero y los trece modulos. Exige sesion y ademas el
 *                 rol Administrador o Farmaceutico.
 *
 * La verificacion del rol es indispensable: sin ella un cliente autenticado
 * podria abrir el modulo de usuarios simplemente escribiendo la direccion.
 */
@WebFilter("/*")
public class FiltroSeguridad implements Filter {

    /** Rutas que cualquiera puede visitar, con o sin cuenta. */
    private static final List<String> RUTAS_PUBLICAS = Arrays.asList(
            "/", "/inicio", "/catalogo", "/producto", "/carrito",
            "/login", "/logout", "/registro",
            "/css/", "/img/", "/favicon.ico");

    /** Rutas reservadas a los perfiles de gestion. */
    private static final List<String> RUTAS_DE_GESTION = Arrays.asList(
            "/panel", "/roles", "/usuarios", "/clientes", "/farmaceuticos", "/productos",
            "/lotes", "/favoritos", "/pedidos", "/lineas", "/formulas", "/historial",
            "/pasarelas", "/transacciones");

    @Override
    public void doFilter(ServletRequest peticionGenerica, ServletResponse respuestaGenerica,
            FilterChain cadena) throws IOException, ServletException {

        HttpServletRequest peticion = (HttpServletRequest) peticionGenerica;
        HttpServletResponse respuesta = (HttpServletResponse) respuestaGenerica;

        String ruta = peticion.getRequestURI().substring(peticion.getContextPath().length());
        Usuario usuario = obtenerUsuarioEnSesion(peticion);

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

        // Zona de gestion: exige rol Administrador o Farmaceutico
        if (esRutaDeGestion(ruta) && !puedeGestionar(usuario)) {
            peticion.getSession().setAttribute(ServletBase.ATRIBUTO_MENSAJE_ERROR,
                    "Su cuenta de cliente no tiene permiso para entrar al panel de gestion.");
            respuesta.sendRedirect(peticion.getContextPath() + "/micuenta");
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
     * Determina si la ruta pertenece al panel de gestion.
     *
     * @param ruta ruta pedida por el navegador
     * @return true cuando la ruta exige un perfil de gestion
     */
    private boolean esRutaDeGestion(String ruta) {
        return RUTAS_DE_GESTION.contains(ruta);
    }

    /**
     * Verifica que el usuario tenga un perfil con acceso al panel.
     *
     * @param usuario usuario autenticado
     * @return true cuando es Administrador o Farmaceutico
     */
    private boolean puedeGestionar(Usuario usuario) {
        return usuario.esAdministrador() || usuario.esFarmaceutico();
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

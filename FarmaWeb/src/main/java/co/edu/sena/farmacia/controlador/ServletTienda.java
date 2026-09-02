package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.util.ConversorDatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador de la tienda publica: pagina de inicio, catalogo y detalle
 * de producto.
 *
 * Es la unica zona del sistema que no exige cuenta. Toda la informacion se
 * pide por el metodo GET, con los parametros categoria, q e id, de modo que
 * cualquier consulta del catalogo se puede compartir como direccion web.
 */
@WebServlet(name = "ServletTienda", urlPatterns = {"/inicio", "/catalogo", "/producto"})
public class ServletTienda extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioProducto servicioProducto = new ServicioProducto();

    /**
     * Atiende las tres pantallas publicas segun la ruta solicitada.
     */
    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        String ruta = peticion.getServletPath();

        // El carrito se crea en la primera visita para que el contador exista
        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("usuario", obtenerUsuario(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);

        try {
            if ("/catalogo".equals(ruta)) {
                mostrarCatalogo(peticion);
                reenviarATienda(peticion, respuesta, "catalogo.jsp");
                return;
            }

            if ("/producto".equals(ruta)) {
                peticion.setAttribute("producto",
                        servicioProducto.consultarPorId(leerEntero(peticion, "id")));
                reenviarATienda(peticion, respuesta, "producto.jsp");
                return;
            }

            mostrarInicio(peticion);

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
            mostrarInicio(peticion);
            reenviarATienda(peticion, respuesta, "inicio.jsp");
            return;
        }

        reenviarATienda(peticion, respuesta, "inicio.jsp");
    }

    /**
     * Prepara la pagina de inicio: categorias con su conteo y destacados.
     *
     * @param peticion peticion HTTP en curso
     */
    private void mostrarInicio(HttpServletRequest peticion) {
        peticion.setAttribute("conteosPorCategoria", servicioProducto.contarPorCategoria());
        peticion.setAttribute("listaProductos", servicioProducto.consultarTodos());
    }

    /**
     * Prepara el catalogo aplicando el filtro por categoria o la busqueda.
     *
     * @param peticion peticion HTTP en curso
     */
    private void mostrarCatalogo(HttpServletRequest peticion) {
        String categoria = ConversorDatos.limpiar(peticion.getParameter("categoria"));
        String busqueda = ConversorDatos.limpiar(peticion.getParameter("q"));

        if (busqueda != null) {
            peticion.setAttribute("listaProductos", servicioProducto.buscarPorTexto(busqueda));
            peticion.setAttribute("tituloCatalogo", "Resultados de la busqueda");

        } else if (categoria != null) {
            peticion.setAttribute("listaProductos", servicioProducto.consultarPorCategoria(categoria));
            peticion.setAttribute("tituloCatalogo", categoria);

        } else {
            peticion.setAttribute("listaProductos", servicioProducto.consultarTodos());
            peticion.setAttribute("tituloCatalogo", "Catalogo de productos");
        }

        peticion.setAttribute("categoriaActiva", categoria);
        peticion.setAttribute("busquedaActiva", busqueda);
    }
}

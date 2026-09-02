package co.edu.sena.farmacia.controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.edu.sena.farmacia.logica.ServicioFavorito;
import co.edu.sena.farmacia.logica.ServicioFormulaMedica;
import co.edu.sena.farmacia.logica.ServicioPedido;
import co.edu.sena.farmacia.logica.ServicioProducto;
import co.edu.sena.farmacia.logica.ServicioRegistro;
import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Favorito;
import co.edu.sena.farmacia.modelo.FormulaMedica;
import co.edu.sena.farmacia.modelo.Pedido;
import co.edu.sena.farmacia.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador de la zona privada del cliente.
 *
 * Muestra sus pedidos, sus favoritos y sus formulas medicas, sin darle
 * acceso a los trece modulos de gestion. Un usuario con rol Administrador o
 * Farmaceutico que llegue aqui es enviado al panel, que es su zona.
 */
@WebServlet(name = "ServletMiCuenta", urlPatterns = {"/micuenta"})
public class ServletMiCuenta extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final transient ServicioRegistro servicioRegistro = new ServicioRegistro();
    private final transient ServicioPedido servicioPedido = new ServicioPedido();
    private final transient ServicioFavorito servicioFavorito = new ServicioFavorito();
    private final transient ServicioFormulaMedica servicioFormula = new ServicioFormulaMedica();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuario(peticion);

        if (usuario == null) {
            redirigirAlModulo(peticion, respuesta, "login");
            return;
        }

        // Los perfiles de gestion tienen su propia pantalla de inicio
        if (usuario.esAdministrador() || usuario.esFarmaceutico()) {
            redirigirAlModulo(peticion, respuesta, "panel");
            return;
        }

        peticion.setAttribute("usuario", usuario);
        peticion.setAttribute("carrito", obtenerCarrito(peticion));
        peticion.setAttribute("categorias", ServicioProducto.CATEGORIAS);

        try {
            Cliente cliente = servicioRegistro.consultarClienteDeUsuario(usuario.getIdUsuario());
            peticion.setAttribute("cliente", cliente);

            if (cliente != null) {
                peticion.setAttribute("misPedidos", filtrarPedidos(cliente.getIdCliente()));
                peticion.setAttribute("misFavoritos", filtrarFavoritos(cliente.getIdCliente()));
                peticion.setAttribute("misFormulas", filtrarFormulas(cliente.getIdCliente()));
            }

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        reenviarATienda(peticion, respuesta, "micuenta.jsp");
    }

    /**
     * Deja unicamente los pedidos que pertenecen al cliente en sesion.
     *
     * @param idCliente identificador del cliente
     * @return pedidos del cliente, del mas reciente al mas antiguo
     */
    private List<Pedido> filtrarPedidos(int idCliente) {
        List<Pedido> mios = new ArrayList<>();

        for (Pedido pedido : servicioPedido.consultarTodos()) {
            if (pedido.getIdCliente() == idCliente) {
                mios.add(pedido);
            }
        }
        return mios;
    }

    /**
     * Deja unicamente los favoritos del cliente en sesion.
     *
     * @param idCliente identificador del cliente
     * @return productos marcados por el cliente
     */
    private List<Favorito> filtrarFavoritos(int idCliente) {
        List<Favorito> mios = new ArrayList<>();

        for (Favorito favorito : servicioFavorito.consultarTodos()) {
            if (favorito.getIdCliente() == idCliente) {
                mios.add(favorito);
            }
        }
        return mios;
    }

    /**
     * Deja unicamente las formulas asociadas a los pedidos del cliente.
     *
     * @param idCliente identificador del cliente
     * @return formulas medicas del cliente
     */
    private List<FormulaMedica> filtrarFormulas(int idCliente) {
        List<FormulaMedica> mias = new ArrayList<>();
        List<Pedido> misPedidos = filtrarPedidos(idCliente);

        for (FormulaMedica formula : servicioFormula.consultarTodos()) {
            for (Pedido pedido : misPedidos) {
                if (formula.getIdPedido() == pedido.getIdPedido()) {
                    mias.add(formula);
                    break;
                }
            }
        }
        return mias;
    }
}

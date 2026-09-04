package co.edu.sena.farmacia.controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.edu.sena.farmacia.logica.CatalogoModulos;
import co.edu.sena.farmacia.logica.ServicioRol;
import co.edu.sena.farmacia.modelo.Rol;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador del modulo de Roles (tabla TblRol).
 *
 * El metodo GET atiende las consultas por parametro (listar, editar y
 * eliminar) y el metodo POST recibe los datos del formulario HTML.
 */
@WebServlet(name = "ServletRol", urlPatterns = {"/roles"})
public class ServletRol extends ServletBase {

    private static final long serialVersionUID = 1L;

    private static final String VISTA = "roles.jsp";
    private static final String RUTA = "roles";

    private final transient ServicioRol servicioRol = new ServicioRol();

    /**
     * Atiende las acciones que llegan como parametros por el metodo GET.
     */
    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        String accion = leerAccion(peticion, "listar");

        try {
            if ("editar".equals(accion)) {
                peticion.setAttribute("rolEnEdicion", servicioRol.consultarPorId(leerEntero(peticion, "id")));

            } else if ("eliminar".equals(accion)) {
                servicioRol.eliminar(leerEntero(peticion, "id"));
                publicarExito(peticion, "El rol fue eliminado correctamente.");
                redirigirAlModulo(peticion, respuesta, RUTA);
                return;
            }

            List<Rol> roles = servicioRol.consultarTodos();
            peticion.setAttribute("listaRoles", roles);

            // Ademas de los permisos escritos, se publica que pestanas ve
            // realmente cada rol segun el catalogo que usa el filtro.
            peticion.setAttribute("modulosPorRol",
                    CatalogoModulos.agruparPorRol(extraerNombres(roles)));

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        reenviarAVista(peticion, respuesta, VISTA);
    }

    /**
     * Toma solo el nombre de cada rol, que es la llave con la que el
     * catalogo de modulos decide el acceso.
     *
     * @param roles roles registrados en la base de datos
     * @return nombres de los roles en el mismo orden
     */
    private List<String> extraerNombres(List<Rol> roles) {
        List<String> nombres = new ArrayList<>();
        for (Rol rol : roles) {
            nombres.add(rol.getNombreRol());
        }
        return nombres;
    }

    /**
     * Recibe por el metodo POST los datos del formulario y guarda el rol.
     */
    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        try {
            Rol rol = new Rol();
            rol.setIdRol(leerEnteroOpcional(peticion, "idRol"));
            rol.setNombreRol(peticion.getParameter("nombreRol"));
            rol.setPermisos(peticion.getParameter("permisos"));

            if (rol.getIdRol() > 0) {
                servicioRol.modificar(rol);
                publicarExito(peticion, "El rol fue actualizado correctamente.");
            } else {
                servicioRol.registrar(rol);
                publicarExito(peticion, "El rol fue registrado correctamente.");
            }

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        redirigirAlModulo(peticion, respuesta, RUTA);
    }
}

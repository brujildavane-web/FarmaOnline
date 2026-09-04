package co.edu.sena.farmacia.logica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.edu.sena.farmacia.modelo.ModuloSistema;

/**
 * Mapa de navegacion del sistema: define cada pestana y los roles que la
 * tienen habilitada.
 *
 * Es la unica fuente de esta informacion en el proyecto y se consulta en
 * tres lugares, de modo que lo que el administrador ve en pantalla es
 * exactamente lo que ocurre en la aplicacion:
 *
 *   1. FiltroSeguridad la usa para autorizar cada peticion.
 *   2. El menu de la cabecera dibuja solo las pestanas del rol en sesion.
 *   3. El modulo de Roles la muestra al administrador como una matriz.
 *
 * Se mantiene en codigo, y no en la base de datos, porque cada ruta existe
 * solo si existe el servlet que la atiende: habilitar una pestana que nadie
 * programo llevaria al usuario a un error 404.
 */
public final class CatalogoModulos {

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_FARMACEUTICO = "Farmaceutico";
    public static final String ROL_CLIENTE = "Cliente";

    /** Zona del panel interno: los modulos derivados del modelo relacional. */
    public static final String ZONA_GESTION = "Gestion";

    /** Zona de la tienda: lo que ve el cliente mientras compra. */
    public static final String ZONA_TIENDA = "Tienda";

    private static final List<String> SOLO_ADMINISTRADOR =
            Arrays.asList(ROL_ADMINISTRADOR);

    private static final List<String> EQUIPO_DE_LA_FARMACIA =
            Arrays.asList(ROL_ADMINISTRADOR, ROL_FARMACEUTICO);

    private static final List<String> TODOS_LOS_ROLES =
            Arrays.asList(ROL_ADMINISTRADOR, ROL_FARMACEUTICO, ROL_CLIENTE);

    private static final List<ModuloSistema> MODULOS = construirCatalogo();

    private CatalogoModulos() {
    }

    /**
     * Arma el catalogo con las pestanas del panel y las de la tienda.
     *
     * El orden es el mismo en que aparecen en el menu de navegacion.
     *
     * @return lista de modulos que no se puede modificar
     */
    private static List<ModuloSistema> construirCatalogo() {
        List<ModuloSistema> catalogo = new ArrayList<>();

        catalogo.add(new ModuloSistema("/panel", "Tablero", ZONA_GESTION,
                "Indicadores generales del negocio y alertas de vencimiento",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/roles", "Roles", ZONA_GESTION,
                "Perfiles de acceso, sus permisos y las pestanas de cada uno",
                true, SOLO_ADMINISTRADOR));
        catalogo.add(new ModuloSistema("/usuarios", "Usuarios", ZONA_GESTION,
                "Cuentas de acceso y asignacion del rol a cada persona",
                true, SOLO_ADMINISTRADOR));
        catalogo.add(new ModuloSistema("/clientes", "Clientes", ZONA_GESTION,
                "Datos de contacto y domicilio de quienes compran",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/farmaceuticos", "Farmaceuticos", ZONA_GESTION,
                "Registro profesional del personal autorizado",
                true, SOLO_ADMINISTRADOR));
        catalogo.add(new ModuloSistema("/productos", "Productos", ZONA_GESTION,
                "Catalogo de medicamentos y productos que ofrece la tienda",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/lotes", "Inventario", ZONA_GESTION,
                "Lotes, existencias disponibles y fechas de vencimiento",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/favoritos", "Favoritos", ZONA_GESTION,
                "Productos que los clientes marcan como preferidos",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/pedidos", "Pedidos", ZONA_GESTION,
                "Pedidos recibidos, su estado y su valor total",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/lineas", "Detalle pedidos", ZONA_GESTION,
                "Productos y cantidades que componen cada pedido",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/formulas", "Formulas medicas", ZONA_GESTION,
                "Recetas que adjuntan los clientes, para validarlas",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/historial", "Historial", ZONA_GESTION,
                "Trazabilidad de los cambios de estado de los pedidos",
                true, EQUIPO_DE_LA_FARMACIA));
        catalogo.add(new ModuloSistema("/pasarelas", "Pasarelas", ZONA_GESTION,
                "Medios de pago habilitados para la tienda",
                true, SOLO_ADMINISTRADOR));
        catalogo.add(new ModuloSistema("/transacciones", "Transacciones", ZONA_GESTION,
                "Pagos registrados, su medio y su estado",
                true, SOLO_ADMINISTRADOR));
        catalogo.add(new ModuloSistema("/reportes", "Reportes", ZONA_GESTION,
                "Reportes de ventas e inventario para analizar el negocio",
                true, SOLO_ADMINISTRADOR));

        catalogo.add(new ModuloSistema("/inicio", "Inicio de la tienda", ZONA_TIENDA,
                "Portada con los productos organizados por categoria",
                false, TODOS_LOS_ROLES));
        catalogo.add(new ModuloSistema("/catalogo", "Catalogo", ZONA_TIENDA,
                "Busqueda de productos y filtro por categoria",
                false, TODOS_LOS_ROLES));
        catalogo.add(new ModuloSistema("/carrito", "Carrito", ZONA_TIENDA,
                "Productos elegidos y adjunto de la receta cuando se exige",
                false, TODOS_LOS_ROLES));
        catalogo.add(new ModuloSistema("/micuenta", "Mi cuenta", ZONA_TIENDA,
                "Datos de la propia cuenta y rol asignado",
                true, TODOS_LOS_ROLES));
        catalogo.add(new ModuloSistema("/mipedido", "Mis pedidos", ZONA_TIENDA,
                "Consulta, edicion o cancelacion de los pedidos propios",
                true, TODOS_LOS_ROLES));
        catalogo.add(new ModuloSistema("/pago", "Pago del pedido", ZONA_TIENDA,
                "Medios de pago: PSE o contraentrega en efectivo o datafono",
                true, TODOS_LOS_ROLES));

        return Collections.unmodifiableList(catalogo);
    }

    /**
     * Filtra los modulos que un rol tiene habilitados.
     *
     * @param nombreRol nombre del rol, tal como esta en TblRol
     * @return modulos visibles para ese rol, en el orden del menu
     */
    public static List<ModuloSistema> modulosDelRol(String nombreRol) {
        List<ModuloSistema> visibles = new ArrayList<>();
        for (ModuloSistema modulo : MODULOS) {
            if (modulo.tieneAcceso(nombreRol)) {
                visibles.add(modulo);
            }
        }
        return visibles;
    }

    /**
     * Filtra los modulos de una zona que un rol tiene habilitados.
     *
     * @param nombreRol nombre del rol
     * @param zona zona buscada: gestion o tienda
     * @return modulos de esa zona visibles para el rol
     */
    public static List<ModuloSistema> modulosDelRol(String nombreRol, String zona) {
        List<ModuloSistema> visibles = new ArrayList<>();
        for (ModuloSistema modulo : modulosDelRol(nombreRol)) {
            if (modulo.getZona().equals(zona)) {
                visibles.add(modulo);
            }
        }
        return visibles;
    }

    /**
     * Agrupa las pestanas de cada rol para mostrarlas como una matriz.
     *
     * @param nombresDeRoles roles registrados en la base de datos
     * @return mapa de rol a modulos visibles, conservando el orden recibido
     */
    public static Map<String, List<ModuloSistema>> agruparPorRol(List<String> nombresDeRoles) {
        Map<String, List<ModuloSistema>> matriz = new LinkedHashMap<>();
        for (String nombreRol : nombresDeRoles) {
            matriz.put(nombreRol, modulosDelRol(nombreRol));
        }
        return matriz;
    }

    /**
     * Busca el modulo que atiende una ruta.
     *
     * @param ruta ruta pedida por el navegador
     * @return el modulo, o null cuando la ruta no esta en el catalogo
     */
    public static ModuloSistema buscarPorRuta(String ruta) {
        for (ModuloSistema modulo : MODULOS) {
            if (modulo.getRuta().equals(ruta)) {
                return modulo;
            }
        }
        return null;
    }
}

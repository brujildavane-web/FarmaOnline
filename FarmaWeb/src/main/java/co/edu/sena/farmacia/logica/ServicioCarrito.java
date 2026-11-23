package co.edu.sena.farmacia.logica;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.sena.farmacia.modelo.Carrito;
import co.edu.sena.farmacia.modelo.Farmaceutico;
import co.edu.sena.farmacia.modelo.FormulaMedica;
import co.edu.sena.farmacia.modelo.Historial;
import co.edu.sena.farmacia.modelo.ItemCarrito;
import co.edu.sena.farmacia.modelo.LineaPedido;
import co.edu.sena.farmacia.modelo.Pedido;
import co.edu.sena.farmacia.modelo.Producto;
import co.edu.sena.farmacia.persistencia.FarmaceuticoDao;
import co.edu.sena.farmacia.persistencia.FormulaMedicaDao;
import co.edu.sena.farmacia.persistencia.HistorialDao;
import co.edu.sena.farmacia.persistencia.LineaPedidoDao;
import co.edu.sena.farmacia.persistencia.PedidoDao;
import co.edu.sena.farmacia.persistencia.ProductoDao;

import java.util.List;

/**
 * Capa de Logica del carrito de compras de la tienda publica.
 *
 * Valida el inventario antes de aceptar un producto y, al confirmar, traduce
 * el carrito de la sesion a un pedido con sus lineas de detalle.
 */
public class ServicioCarrito {

    private final ServicioNotificacion servicioNotificacion = new ServicioNotificacion();

    /** Estado con que nace todo pedido creado por el propio cliente. */
    public static final String ESTADO_INICIAL = "PENDIENTE";

    private final ProductoDao productoDao = new ProductoDao();
    private final PedidoDao pedidoDao = new PedidoDao();
    private final LineaPedidoDao lineaPedidoDao = new LineaPedidoDao();
    private final FarmaceuticoDao farmaceuticoDao = new FarmaceuticoDao();
    private final HistorialDao historialDao = new HistorialDao();
    private final FormulaMedicaDao formulaMedicaDao = new FormulaMedicaDao();

    /**
     * Agrega un producto al carrito verificando que haya inventario.
     *
     * @param carrito carrito de la sesion
     * @param idProducto identificador del producto escogido
     * @param cantidad unidades solicitadas
     * @return nombre del producto agregado, para el mensaje de confirmacion
     */
    public String agregarProducto(Carrito carrito, int idProducto, double cantidad) {
        if (cantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad debe ser mayor que cero.");
        }

        Producto producto = productoDao.buscarPorId(idProducto);
        if (producto == null) {
            throw new ExcepcionNegocio("El producto seleccionado no existe en el catalogo.");
        }

        double disponible = productoDao.consultarStockDisponible(idProducto);
        ItemCarrito existente = carrito.buscarItem(idProducto);
        double yaEnCarrito = existente == null ? 0 : existente.getCantidad();

        if (disponible < yaEnCarrito + cantidad) {
            throw new ExcepcionNegocio("Solo hay " + disponible + " unidades disponibles de "
                    + producto.getNombre() + ".");
        }

        carrito.agregar(producto, cantidad);
        return producto.getNombre();
    }

    /**
     * Cambia la cantidad de un renglon del carrito.
     *
     * @param carrito carrito de la sesion
     * @param idProducto identificador del producto
     * @param cantidad cantidad nueva
     */
    public void cambiarCantidad(Carrito carrito, int idProducto, double cantidad) {
        if (cantidad > 0) {
            double disponible = productoDao.consultarStockDisponible(idProducto);
            if (disponible < cantidad) {
                throw new ExcepcionNegocio("Solo hay " + disponible + " unidades disponibles.");
            }
        }
        carrito.cambiarCantidad(idProducto, cantidad);
    }

    /**
     * Adjunta la formula medica a un renglon del carrito.
     *
     * El archivo ya fue guardado en disco; aqui solo se registra su nombre y
     * la fecha de prescripcion en la sesion, porque TblFormulaMedica exige un
     * pedido que todavia no existe.
     *
     * @param carrito carrito de la sesion
     * @param idProducto producto controlado al que corresponde la formula
     * @param nombreArchivo nombre con que quedo guardado el archivo
     * @param fechaPrescripcion fecha en que el medico expidio la formula
     * @return nombre del producto, para el mensaje de confirmacion
     */
    public String adjuntarReceta(Carrito carrito, int idProducto, String nombreArchivo,
            LocalDate fechaPrescripcion) {

        ItemCarrito item = carrito.buscarItem(idProducto);
        if (item == null) {
            throw new ExcepcionNegocio("El producto no esta en su carrito.");
        }
        if (!item.isRequiereReceta()) {
            throw new ExcepcionNegocio(
                    item.getNombre() + " es de venta libre y no necesita formula medica.");
        }
        if (fechaPrescripcion == null) {
            throw new ExcepcionNegocio("Debe indicar la fecha en que se expidio la formula.");
        }
        if (fechaPrescripcion.isAfter(LocalDate.now())) {
            throw new ExcepcionNegocio("La fecha de la formula no puede ser posterior a hoy.");
        }

        item.setArchivoReceta(nombreArchivo);
        item.setFechaPrescripcion(fechaPrescripcion);
        return item.getNombre();
    }

    /**
     * Quita la formula adjunta de un renglon, para poder reemplazarla.
     *
     * @param carrito carrito de la sesion
     * @param idProducto producto al que se le retira la formula
     */
    public void quitarReceta(Carrito carrito, int idProducto) {
        ItemCarrito item = carrito.buscarItem(idProducto);
        if (item != null) {
            item.setArchivoReceta(null);
            item.setFechaPrescripcion(null);
        }
    }

    /**
     * Confirma la compra: crea el pedido, sus lineas y el primer registro
     * del historial, y deja el carrito vacio.
     *
     * El farmaceutico se asigna automaticamente porque la columna
     * TblFar_FarIdFarmaceutico de TblPedido no admite nulos y el cliente no
     * escoge quien lo atiende. El pedido nace en estado PENDIENTE para que
     * un farmaceutico lo revise y lo confirme despues.
     *
     * @param carrito carrito de la sesion
     * @param idCliente cliente autenticado que confirma la compra
     * @return identificador del pedido creado
     */
    public int confirmarPedido(Carrito carrito, int idCliente) {
        if (carrito == null || carrito.estaVacio()) {
            throw new ExcepcionNegocio("Su carrito esta vacio.");
        }
        if (idCliente <= 0) {
            throw new ExcepcionNegocio("Debe ingresar con su cuenta para confirmar el pedido.");
        }

        verificarInventarioCompleto(carrito);
        verificarRecetasCompletas(carrito);

        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(carrito.calcularTotal());
        pedido.setEstado(ESTADO_INICIAL);
        pedido.setIdCliente(idCliente);
        pedido.setIdFarmaceutico(obtenerFarmaceuticoDisponible());

        pedidoDao.insertar(pedido);

        int idPedido = ubicarPedidoRecienCreado(idCliente);
        guardarLineas(carrito, idPedido);
        guardarFormulasMedicas(carrito, idPedido);
        registrarHistorial(carrito, idPedido);

        carrito.vaciar();

        servicioNotificacion.notificarCliente(idCliente,
                "Su pedido " + idPedido + " fue registrado y esta pendiente de confirmacion.");
        return idPedido;
    }

    /**
     * Vuelve a comprobar el inventario de todos los renglones justo antes de
     * confirmar, porque el stock pudo cambiar mientras el cliente compraba.
     */
    private void verificarInventarioCompleto(Carrito carrito) {
        for (ItemCarrito item : carrito.getItems()) {
            double disponible = productoDao.consultarStockDisponible(item.getIdProducto());
            if (disponible < item.getCantidad()) {
                throw new ExcepcionNegocio("El inventario de " + item.getNombre()
                        + " cambio: solo quedan " + disponible + " unidades. Ajuste su carrito.");
            }
        }
    }

    /**
     * Impide confirmar la compra si algun medicamento controlado no tiene su
     * formula medica adjunta.
     *
     * Es la regla que exige receta valida antes de despachar medicamentos
     * controlados, aplicada en el momento de la compra.
     */
    private void verificarRecetasCompletas(Carrito carrito) {
        List<ItemCarrito> pendientes = carrito.obtenerItemsSinReceta();

        if (pendientes.isEmpty()) {
            return;
        }

        StringBuilder nombres = new StringBuilder();
        for (ItemCarrito item : pendientes) {
            if (nombres.length() > 0) {
                nombres.append(", ");
            }
            nombres.append(item.getNombre());
        }

        throw new ExcepcionNegocio("Debe adjuntar la formula medica de: " + nombres
                + ". Sin ella no se puede comprar un medicamento controlado.");
    }

    /**
     * Crea una fila de TblFormulaMedica por cada medicamento controlado del
     * pedido, con el archivo que el cliente adjunto en el carrito.
     */
    private void guardarFormulasMedicas(Carrito carrito, int idPedido) {
        for (ItemCarrito item : carrito.getItems()) {
            if (!item.isRequiereReceta()) {
                continue;
            }

            FormulaMedica formula = new FormulaMedica();
            formula.setIdPedido(idPedido);
            formula.setIdProducto(item.getIdProducto());
            formula.setArchivo(item.getArchivoReceta());
            formula.setFechaPrescripcion(item.getFechaPrescripcion());

            formulaMedicaDao.insertar(formula);
        }
    }

    /**
     * Escoge el farmaceutico que atendera el pedido.
     *
     * @return identificador del primer farmaceutico registrado
     */
    private int obtenerFarmaceuticoDisponible() {
        List<Farmaceutico> farmaceuticos = farmaceuticoDao.listar();

        if (farmaceuticos.isEmpty()) {
            throw new ExcepcionNegocio(
                    "No hay farmaceuticos registrados para atender el pedido. "
                    + "Comuniquese con la farmacia.");
        }
        return farmaceuticos.get(0).getIdFarmaceutico();
    }

    /**
     * Ubica el pedido que se acaba de insertar para el cliente.
     *
     * Se busca el mas reciente del cliente porque el DAO no devuelve la
     * llave generada por la base de datos.
     *
     * @param idCliente cliente que confirmo la compra
     * @return identificador del pedido mas reciente del cliente
     */
    private int ubicarPedidoRecienCreado(int idCliente) {
        int idPedido = 0;

        for (Pedido pedido : pedidoDao.listar()) {
            if (pedido.getIdCliente() == idCliente && pedido.getIdPedido() > idPedido) {
                idPedido = pedido.getIdPedido();
            }
        }

        if (idPedido == 0) {
            throw new ExcepcionNegocio("El pedido se registro pero no fue posible recuperarlo.");
        }
        return idPedido;
    }

    /** Convierte cada renglon del carrito en una linea del pedido. */
    private void guardarLineas(Carrito carrito, int idPedido) {
        for (ItemCarrito item : carrito.getItems()) {
            LineaPedido linea = new LineaPedido();
            linea.setIdPedido(idPedido);
            linea.setIdProducto(item.getIdProducto());
            linea.setCantidad(item.getCantidad());
            linea.setPrecio(item.getPrecio());
            linea.setSubtotal(item.calcularSubtotal());

            lineaPedidoDao.insertar(linea);
        }
    }

    /** Deja la primera huella del pedido en la bitacora de seguimiento. */
    private void registrarHistorial(Carrito carrito, int idPedido) {
        for (ItemCarrito item : carrito.getItems()) {
            Historial registro = new Historial();
            registro.setIdPedido(idPedido);
            registro.setIdProducto(item.getIdProducto());
            registro.setFecha(LocalDate.now());
            registro.setEstado(ESTADO_INICIAL);

            historialDao.insertar(registro);
        }
    }
}

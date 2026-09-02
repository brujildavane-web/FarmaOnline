package co.edu.sena.farmacia.logica;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import co.edu.sena.farmacia.modelo.Producto;
import co.edu.sena.farmacia.persistencia.ProductoDao;
import co.edu.sena.farmacia.util.ConversorDatos;

/**
 * Capa de Logica del modulo de Productos y Catalogo.
 *
 * Valida el precio, exige que el codigo SKU sea unico y expone la consulta
 * de stock que utilizan los modulos de pedidos e inventario.
 */
public class ServicioProducto {

    /** Categorias con que se clasifica el catalogo de la tienda. */
    public static final List<String> CATEGORIAS = Arrays.asList(
            "Medicamentos", "Cuidado personal", "Botiquin", "Bebes", "Nutricion");

    private final ProductoDao productoDao = new ProductoDao();

    /**
     * Consulta el catalogo completo con el stock vigente de cada producto.
     *
     * @return lista de productos ordenada por nombre
     */
    public List<Producto> consultarTodos() {
        return productoDao.listar();
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param idProducto identificador del producto
     * @return el producto encontrado
     */
    public Producto consultarPorId(int idProducto) {
        Producto producto = productoDao.buscarPorId(idProducto);
        if (producto == null) {
            throw new ExcepcionNegocio("El producto solicitado no existe.");
        }
        return producto;
    }

    /**
     * Consulta los productos de una categoria para el catalogo publico.
     *
     * @param categoria nombre de la categoria
     * @return productos de esa categoria
     */
    public List<Producto> consultarPorCategoria(String categoria) {
        if (ConversorDatos.estaVacio(categoria)) {
            return consultarTodos();
        }
        return productoDao.listarPorCategoria(categoria.trim());
    }

    /**
     * Busca productos por nombre o codigo para el buscador de la tienda.
     *
     * @param texto palabra o fragmento a buscar
     * @return productos que coinciden
     */
    public List<Producto> buscarPorTexto(String texto) {
        if (ConversorDatos.estaVacio(texto)) {
            return consultarTodos();
        }
        return productoDao.buscarPorTexto(texto.trim());
    }

    /**
     * Cuenta los productos que hay en cada categoria.
     *
     * Alimenta las tarjetas de categoria de la pagina de inicio.
     *
     * @return nombre de la categoria y su cantidad de productos
     */
    public Map<String, Integer> contarPorCategoria() {
        return productoDao.contarPorCategoria();
    }

    /**
     * Busca un producto por su codigo SKU o de barras.
     *
     * @param skuCode codigo del producto
     * @return el producto encontrado
     */
    public Producto consultarPorSku(String skuCode) {
        if (ConversorDatos.estaVacio(skuCode)) {
            throw new ExcepcionNegocio("Debe indicar el codigo del producto.");
        }

        Producto producto = productoDao.buscarPorSku(skuCode.trim());
        if (producto == null) {
            throw new ExcepcionNegocio("No existe un producto con el codigo " + skuCode + ".");
        }
        return producto;
    }

    /**
     * Consulta las unidades disponibles de un producto.
     *
     * @param idProducto identificador del producto
     * @return unidades vigentes en los lotes no vencidos
     */
    public double consultarStockDisponible(int idProducto) {
        return productoDao.consultarStockDisponible(idProducto);
    }

    /**
     * Registra un producto nuevo en el catalogo.
     *
     * @param producto datos del producto
     */
    public void registrar(Producto producto) {
        validar(producto);
        verificarSkuDisponible(producto.getSkuCode(), 0);
        productoDao.insertar(producto);
    }

    /**
     * Modifica un producto existente del catalogo.
     *
     * @param producto datos nuevos del producto
     */
    public void modificar(Producto producto) {
        validar(producto);
        verificarSkuDisponible(producto.getSkuCode(), producto.getIdProducto());
        productoDao.actualizar(producto);
    }

    /**
     * Elimina un producto del catalogo.
     *
     * @param idProducto identificador del producto
     */
    public void eliminar(int idProducto) {
        productoDao.eliminar(idProducto);
    }

    /**
     * Cuenta los productos del catalogo.
     *
     * @return cantidad de productos
     */
    public int contar() {
        return productoDao.contar();
    }

    private void validar(Producto producto) {
        if (ConversorDatos.estaVacio(producto.getNombre())) {
            throw new ExcepcionNegocio("El nombre del producto es obligatorio.");
        }
        if (producto.getPrecio() == null) {
            throw new ExcepcionNegocio("El precio del producto es obligatorio.");
        }
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionNegocio("El precio del producto debe ser mayor que cero.");
        }

        validarCategoria(producto.getCategoria());
        validarImagen(producto.getImagen());
    }

    /**
     * Comprueba que la categoria sea una de las definidas para el catalogo.
     *
     * @param categoria categoria escogida; si viene vacia no se valida
     */
    private void validarCategoria(String categoria) {
        if (ConversorDatos.estaVacio(categoria)) {
            return;
        }
        if (!CATEGORIAS.contains(categoria.trim())) {
            throw new ExcepcionNegocio("La categoria " + categoria
                    + " no es valida. Use una de estas: " + CATEGORIAS + ".");
        }
    }

    /**
     * Comprueba que el nombre del archivo de imagen tenga un formato de foto.
     *
     * @param nombreArchivo nombre del archivo; si viene vacio no se valida
     */
    private void validarImagen(String nombreArchivo) {
        if (ConversorDatos.estaVacio(nombreArchivo)) {
            return;
        }

        String minusculas = nombreArchivo.trim().toLowerCase();
        boolean formatoValido = minusculas.endsWith(".png")
                || minusculas.endsWith(".jpg")
                || minusculas.endsWith(".jpeg")
                || minusculas.endsWith(".webp");

        if (!formatoValido) {
            throw new ExcepcionNegocio(
                    "La imagen del producto debe ser un archivo PNG, JPG o WEBP.");
        }
    }

    /**
     * Impide que dos productos compartan el mismo codigo SKU.
     *
     * @param skuCode codigo a verificar; si viene vacio no se valida
     * @param idProductoActual identificador del producto que se edita, o 0 si es nuevo
     */
    private void verificarSkuDisponible(String skuCode, int idProductoActual) {
        if (ConversorDatos.estaVacio(skuCode)) {
            return;
        }

        Producto existente = productoDao.buscarPorSku(skuCode.trim());
        if (existente != null && existente.getIdProducto() != idProductoActual) {
            throw new ExcepcionNegocio("El codigo SKU " + skuCode + " ya esta asignado a otro producto.");
        }
    }
}
